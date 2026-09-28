$ErrorActionPreference = "Continue"
$log = "c:\Users\Luisv\OneDrive\Desktop\CS2\nexusmarket\nexusmarketII\smoke-results.log"
"=== SMOKE TEST RESULTS ===" | Out-File $log

$proc = Start-Process -FilePath "cmd.exe" -ArgumentList '/c set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot" && mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=demo,memory -Dspring-boot.run.jvmArguments="-Dserver.port=8090"' -WorkingDirectory "c:\Users\Luisv\OneDrive\Desktop\CS2\nexusmarket\nexusmarketII" -WindowStyle Hidden -PassThru

function Req($method, $url, $body) {
    try {
        if ($method -eq "GET") {
            $r = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 15
        } else {
            $r = Invoke-RestMethod -Uri $url -Method $method -Body ($body | ConvertTo-Json) -ContentType "application/json" -TimeoutSec 15
        }
        return $r | ConvertTo-Json -Depth 6 -Compress
    } catch {
        $resp = $_.Exception.Response
        if ($resp) {
            try {
                $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
                return "HTTP " + [int]$resp.StatusCode + ": " + $reader.ReadToEnd()
            } catch { return "HTTP " + [int]$resp.StatusCode }
        }
        return "PENDING/ERROR: " + $_.Exception.Message
    }
}

$up = $false
for ($i = 0; $i -lt 40; $i++) {
    Start-Sleep -Seconds 3
    $probe = Req "GET" "http://localhost:8090/api/users/administrator-1?requesterId=administrator-1" $null
    if ($probe -notmatch "PENDING/ERROR") { $up = $true; break }
}
"SERVER UP: $up" | Out-File $log -Append

"--- 1. GET /api/users/administrator-1 ---" | Out-File $log -Append
(Req "GET" "http://localhost:8090/api/users/administrator-1?requesterId=administrator-1" $null) | Out-File $log -Append

"--- 2. POST /api/users (register logistics operator) ---" | Out-File $log -Append
(Req "POST" "http://localhost:8090/api/users" @{ performerId="administrator-1"; identifier="logistics-operator-1"; fullName="Luis Operador"; email="logistics@nexusmarket.com"; roleCode="LOGISTICS_OPERATOR" }) | Out-File $log -Append

"--- 3. POST /api/inventory/movements (stock-in x10) ---" | Out-File $log -Append
(Req "POST" "http://localhost:8090/api/inventory/movements" @{ performerId="logistics-operator-1"; productId="product-1"; warehouseId="warehouse-1"; quantity=10; movementType="STOCK_IN" }) | Out-File $log -Append

"--- 4. GET /api/inventory ---" | Out-File $log -Append
(Req "GET" "http://localhost:8090/api/inventory?requesterId=buyer-1&productId=product-1&warehouseId=warehouse-1" $null) | Out-File $log -Append

"--- 5. POST /api/carts/items (add 2x product-1) ---" | Out-File $log -Append
(Req "POST" "http://localhost:8090/api/carts/items" @{ buyerId="buyer-1"; cartId=""; productId="product-1"; quantity=2; unitPrice=50.00 }) | Out-File $log -Append

"--- 6. GET /api/carts ---" | Out-File $log -Append
(Req "GET" "http://localhost:8090/api/carts?buyerId=buyer-1" $null) | Out-File $log -Append

"--- 7. POST /api/orders (checkout) ---" | Out-File $log -Append
$orderJson = Req "POST" "http://localhost:8090/api/orders" @{ buyerId="buyer-1"; cartId="" }
$orderJson | Out-File $log -Append
$orderId = ""
try { $orderId = ($orderJson | ConvertFrom-Json).data.orderId } catch {}
"ORDER ID: $orderId" | Out-File $log -Append

if ($orderId) {
    "--- 8. POST /api/orders/$orderId/payment ---" | Out-File $log -Append
    (Req "POST" "http://localhost:8090/api/orders/$orderId/payment" @{ performerId="buyer-1" }) | Out-File $log -Append

    "--- 9. POST /api/shipments ---" | Out-File $log -Append
    (Req "POST" "http://localhost:8090/api/shipments" @{ operatorId="logistics-operator-1"; orderId=$orderId; originWarehouseId="warehouse-1" }) | Out-File $log -Append

    "--- 10. POST /api/shipments/$orderId/dispatch ---" | Out-File $log -Append
    (Req "POST" "http://localhost:8090/api/shipments/$orderId/dispatch" @{ operatorId="logistics-operator-1" }) | Out-File $log -Append

    "--- 11. POST /api/shipments/$orderId/delivery ---" | Out-File $log -Append
    (Req "POST" "http://localhost:8090/api/shipments/$orderId/delivery" @{ operatorId="logistics-operator-1" }) | Out-File $log -Append

    "--- 12. GET /api/shipments/$orderId ---" | Out-File $log -Append
    (Req "GET" "http://localhost:8090/api/shipments/$orderId?requesterId=buyer-1" $null) | Out-File $log -Append

        "--- 13. GET /api/invoices/$orderId ---" | Out-File $log -Append
    (Req "GET" "http://localhost:8090/api/invoices/$orderId?requesterId=buyer-1" $null) | Out-File $log -Append
}

"--- 14. GET /api/reports/commercial ---" | Out-File $log -Append
(Req "GET" "http://localhost:8090/api/reports/commercial?requesterId=administrator-1" $null) | Out-File $log -Append

"--- 15. GET /api/reports/sellers/seller-1 ---" | Out-File $log -Append
(Req "GET" "http://localhost:8090/api/reports/sellers/seller-1?requesterId=administrator-1" $null) | Out-File $log -Append

"--- 16. POST /api/authorization/permissions (buyer vs REGISTER_SELLER -> denied) ---" | Out-File $log -Append
(Req "POST" "http://localhost:8090/api/authorization/permissions" @{ userId="buyer-1"; operation="REGISTER_SELLER" }) | Out-File $log -Append

if ($orderId) {
    "--- 17. POST /api/authorization/ownership (buyer owns its order -> allowed) ---" | Out-File $log -Append
    (Req "POST" "http://localhost:8090/api/authorization/ownership" @{ userId="buyer-1"; resourceType="ORDER"; resourceId=$orderId }) | Out-File $log -Append
}

"--- 18. POST /api/warehouses (seller warehouse) ---" | Out-File $log -Append
(Req "POST" "http://localhost:8090/api/warehouses" @{ performerId="seller-1"; warehouseId="warehouse-2"; name="Seller Storage"; street="Cra 45"; city="Medellin"; state="ANT"; country="Colombia"; postalCode="050001"; ownership="SELLER" }) | Out-File $log -Append

"--- 19. GET /api/warehouses/warehouse-2 ---" | Out-File $log -Append
(Req "GET" "http://localhost:8090/api/warehouses/warehouse-2?requesterId=administrator-1" $null) | Out-File $log -Append

"--- 20. PUT /api/warehouses/warehouse-2 (rename) ---" | Out-File $log -Append
(Req "PUT" "http://localhost:8090/api/warehouses/warehouse-2" @{ performerId="seller-1"; newName="Seller Storage II" }) | Out-File $log -Append

"--- 21. POST /api/buyers ---" | Out-File $log -Append
(Req "POST" "http://localhost:8090/api/buyers" @{ identifier="buyer-2"; fullName="Ana Compradora"; email="ana@nexusmarket.com"; street="Calle 9"; city="Cali"; state="VAC"; country="Colombia"; postalCode="760001" }) | Out-File $log -Append

"--- 22. GET /api/buyers/buyer-1/activity ---" | Out-File $log -Append
(Req "GET" "http://localhost:8090/api/buyers/buyer-1/activity?requesterId=administrator-1" $null) | Out-File $log -Append

"--- 23. PUT /api/products/product-1 (update description) ---" | Out-File $log -Append
(Req "PUT" "http://localhost:8090/api/products/product-1" @{ performerId="seller-1"; newDescription="RGB keyboard pro" }) | Out-File $log -Append

"--- 24. POST /api/products/product-1/status (SUSPENDED) ---" | Out-File $log -Append
(Req "POST" "http://localhost:8090/api/products/product-1/status" @{ performerId="seller-1"; statusCode="SUSPENDED" }) | Out-File $log -Append

"--- 25. GET /api/sellers/seller-1 ---" | Out-File $log -Append
(Req "GET" "http://localhost:8090/api/sellers/seller-1?requesterId=administrator-1" $null) | Out-File $log -Append

"--- KILLING APP ---" | Out-File $log -Append
try {
    Get-NetTCPConnection -LocalPort 8090 -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique |
        ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }
} catch {}
Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
"=== DONE ===" | Out-File $log -Append
