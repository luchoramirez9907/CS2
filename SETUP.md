# Guía paso a paso: poner en marcha NexusMarket

Probado en Windows 11 + PowerShell. Los comandos de Maven se ejecutan desde
`nexusmarket\nexusmarketII`; los de Docker, desde la raíz del repositorio.

## 0. Prerrequisitos

1. **JDK 17 o superior**: `java -version`.
2. **Docker Desktop** solo si se va a usar MySQL/MongoDB reales (opciones B y C del paso 3).
3. Puerto libre para la aplicación (por defecto **8080**). Si está ocupado, usar `SERVER_PORT`.

## 1. Compilar y correr las pruebas

```powershell
cd nexusmarket\nexusmarketII
.\mvnw.cmd test
```

Resultado esperado: `Tests run: 112, Failures: 0, Errors: 0` y `BUILD SUCCESS`.
Las pruebas **no necesitan bases de datos**: usan el perfil `memory`.

## 2. Empaquetar

```powershell
.\mvnw.cmd -q -DskipTests package
```

Genera `target\nexusmarket-0.0.1-SNAPSHOT.jar`.

## 3. Arrancar la aplicación

### Opción A — sin bases de datos (perfil `memory`)

Persistencia en memoria y datos de ejemplo (`demo`):

```powershell
java -jar target\nexusmarket-0.0.1-SNAPSHOT.jar --spring.profiles.active=memory,demo
```

Los datos se pierden al detener la aplicación.

### Opción B — con MySQL y MongoDB (Docker)

Desde la raíz del repositorio:

```powershell
docker compose up -d
docker ps --format "{{.Names}}|{{.Status}}"
```

Crea `nexusmarket-mysql` (MySQL 8, base `nexusmarket`, usuario `root` / clave `root_password`)
y `nexusmarket-mongo` (MongoDB 6). Luego, desde `nexusmarket\nexusmarketII`:

```powershell
java -jar target\nexusmarket-0.0.1-SNAPSHOT.jar --spring.profiles.active=demo
```

Hibernate crea las tablas al arrancar y MongoDB crea las colecciones con el primer documento.

### Opción C — todo en Docker (aplicación + MySQL + MongoDB)

Desde la raíz del repositorio, sin instalar Java ni Maven:

```powershell
docker compose up --build
```

Levanta tres contenedores: `nexusmarket-mysql`, `nexusmarket-mongo` y `nexusmarket-app`.
La aplicación espera a que las dos bases de datos estén sanas (`healthcheck`) y arranca con
el perfil `demo` en `http://127.0.0.1:8080`. Se conecta a ellas por la red interna de Docker
(`mysql-db:3306` con el usuario `nexusmarket_app`, `mongo-db:27017`). El código fuente se monta
como volumen y las dependencias de Maven quedan en el volumen `maven_cache`, así que el
segundo arranque es más rápido. Los archivos compilados del contenedor van en su propio volumen
(`app_target`), separado de la carpeta `target` que usa VS Code, para que no se pisen. El puerto publicado se cambia con `HOST_SERVER_PORT`.

### Verificar salud

Toda petición pide token salvo el login y el registro de comprador, así que el health también lo pide:

```powershell
curl.exe -s -o NUL -w "HTTP:%{http_code}`n" http://127.0.0.1:8080/actuator/health
# HTTP:401 (sin token: la API está viva y la seguridad responde)
```

Con el token del paso 5 responde `{"status":"UP"}`:

```powershell
curl.exe -s http://127.0.0.1:8080/actuator/health -H "Authorization: Bearer $buyer"
```

## 4. Datos de demostración (perfil `demo`)

| Cuenta | Rol | Identificador |
|---|---|---|
| `admin@nexusmarket.com` | ADMINISTRATOR | `administrator-1` |
| `seller@nexusmarket.com` | SELLER | `seller-1` |
| `buyer@nexusmarket.com` | BUYER | `buyer-1` |
| `supervisor@nexusmarket.com` | SUPERVISOR | `supervisor-1` |
| `operator@nexusmarket.com` | LOGISTICS_OPERATOR | `operator-1` |

Además: bodega `warehouse-1` y producto `product-1` (50 unidades). La contraseña de las
cinco cuentas es el valor de `nexusmarket.demo.password` en `application.properties`
(se puede cambiar con la variable de entorno `DEMO_PASSWORD` antes de arrancar).

## 5. Prueba de humo (login → compra → logout)

> En PowerShell, pasar JSON inline a `curl.exe` suele romper las comillas: usar archivos.

1. Crear los bodies (sustituir `<password>` por la contraseña de demostración):
   ```powershell
   $tmp = "$env:TEMP\nexusmarket-smoke"
   New-Item -ItemType Directory -Path $tmp -Force | Out-Null
   Set-Content "$tmp\buyer.json" -Value '{"email":"buyer@nexusmarket.com","password":"<password>"}' -NoNewline
   Set-Content "$tmp\admin.json" -Value '{"email":"admin@nexusmarket.com","password":"<password>"}' -NoNewline
   Set-Content "$tmp\item.json"  -Value '{"productId":"product-1","quantity":2}' -NoNewline
   ```
2. Login del comprador → **200** con `token`:
   ```powershell
   $api = "http://127.0.0.1:8080/api/v1"
   $buyer = (curl.exe -s -X POST "$api/auth/login" -H "Content-Type: application/json" -d "@$tmp\buyer.json" | ConvertFrom-Json).data.token
   ```
3. Sin token → **401**; con token → **200**; ruta de otro rol → **403**:
   ```powershell
   curl.exe -s -o NUL -w "HTTP:%{http_code}`n" "$api/buyer/profile"
   curl.exe -s -o NUL -w "HTTP:%{http_code}`n" "$api/buyer/profile" -H "Authorization: Bearer $buyer"
   curl.exe -s -o NUL -w "HTTP:%{http_code}`n" "$api/admin/users/administrator-1" -H "Authorization: Bearer $buyer"
   ```
4. Carrito y checkout → **201**:
   ```powershell
   curl.exe -s -X POST "$api/buyer/cart/items" -H "Authorization: Bearer $buyer" -H "Content-Type: application/json" -d "@$tmp\item.json"
   $orderId = (curl.exe -s -X POST "$api/buyer/orders" -H "Authorization: Bearer $buyer" | ConvertFrom-Json).data.orderId
   ```
5. El administrador confirma el pago → `orderStatus: PAID`; el comprador consulta su factura (IVA incluido):
   ```powershell
   $admin = (curl.exe -s -X POST "$api/auth/login" -H "Content-Type: application/json" -d "@$tmp\admin.json" | ConvertFrom-Json).data.token
   curl.exe -s -X POST "$api/admin/orders/$orderId/payment" -H "Authorization: Bearer $admin"
   curl.exe -s "$api/buyer/orders/$orderId/invoice" -H "Authorization: Bearer $buyer"
   ```
6. Logout → **204**; el token anterior queda invalidado → **401**:
   ```powershell
   curl.exe -s -o NUL -w "HTTP:%{http_code}`n" -X POST "$api/auth/logout" -H "Authorization: Bearer $buyer"
   curl.exe -s -o NUL -w "HTTP:%{http_code}`n" "$api/buyer/profile" -H "Authorization: Bearer $buyer"
   ```

El catálogo completo de endpoints está en `SDD/Adapters/Api-rest-endpoints.md`.

## 6. Apagar todo

```powershell
# Detener la aplicación: Ctrl+C en su terminal (también en la opción C)
docker compose stop        # o `docker compose down` para borrar los contenedores (los volúmenes se conservan)
```

## 7. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Port 8080 was already in use` | Otro proceso ocupa el puerto | `$env:SERVER_PORT="8081"` antes de `java -jar` |
| `Communications link failure` al arrancar sin perfil `memory` | MySQL apagado | `docker compose up -d`, o arrancar con `--spring.profiles.active=memory,demo` |
| `401` en todas las rutas protegidas | Token ausente, vencido o invalidado por logout / cambio de contraseña | Volver a hacer login |
| `403 FORBIDDEN` | La ruta pertenece a otro rol, o el recurso es de otro participante | Usar la cuenta del rol correspondiente |
| `mvnw clean` falla borrando `target` | El servidor Java de VS Code tiene archivos abiertos | Recargar la ventana de VS Code o cerrar la carpeta y repetir |
| `curl.exe` devuelve 400 con JSON inline en PowerShell | Comillas alteradas por la shell | Usar `-d "@archivo.json"` como en el paso 5 |
| `nexusmarket-app` se reinicia sin parar con `ClassFormatError` en el log | Archivos compilados dañados en `target` | El contenedor ya compila en su propio volumen (`app_target`), separado del `target` de VS Code. Si pasa igual: `docker compose down nexusmarket-app`, `docker volume rm cs2_app_target` y `docker compose up -d` |
