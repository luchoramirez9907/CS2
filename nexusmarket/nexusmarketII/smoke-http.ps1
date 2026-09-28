$ErrorActionPreference = "Continue"
$log = "c:\Users\Luisv\OneDrive\Desktop\CS2\nexusmarket\nexusmarketII\smoke-results.log"
$base = "http://localhost:8090"

"=== SMOKE TEST RESULTS " + (Get-Date -Format "yyyy-MM-dd HH:mm:ss") + " ===" | Out-File $log -Encoding utf8

function Req($method, $url, $body) {
    try {
        if ($method -eq "GET") {
            $r = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 20
        } else {
            $r = Invoke-RestMethod -Uri $url -Method $method -Body ($body | ConvertTo-Json) -ContentType "application/json" -TimeoutSec 20
        }
        return $r | ConvertTo-Json -Depth 6 -Compress
    } catch {
        $resp = $_.Exception.Response
        if ($resp) {
            $status = [int]$resp.StatusCode
            $detail = ""
            try {
                $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
                $detail = $reader.ReadToEnd()
            } catch {}
            return "HTTP $status : $detail"
        }
        return "CONN-ERROR: " + $_.Exception.Message
    }
}

# wait for server
$up = $false
for ($i = 0; $i -lt 30; $i++) {
    Start-Sleep -Seconds 4
    $probe = Req "GET" "$base/api/users/administrator-1?requesterId=administrator-1" $null
    if ($probe -notmatch "CONN-ERROR") { $up = $true; break }
}
"SERVER UP: $up" | Out-File $log -Append -Encoding utf8
if (-not $up) { "=== ABORTED (server never came up) ===" | Out-File $log -Append -Encoding utf8; exit 1 }

"--- 1. GET /api/users/administrator-1 ---" | Out-File $log -Append -Encoding utf8
(Req "GET" "$base/api/users/administrator-1?requesterId=administrator-1" $null) | Out-File $log -Append -Encoding utf8

"--- 2. POST /api/users (register LOGISTICS_OPERATOR by admin) ---" | Out-File $log -Append -Encoding utf8
(Req "POST" "$base/api/users" @{ performerId="administrator-1"; identifier="logistics-operator-1"; fullName="Luis Operador"; email="logistics@nexusmarket.com"; roleCode="LOGISTICS_OPERATOR" }) | Out-File $log -Append -Encoding utf8

"--- 3. POST /api/inventory/movements (STOCK_IN x10 by operator) ---" | Out-File $log -Append -Encoding utf8
(Req "POST" "$base/api/inventory/movements" @{ performerId="logistics-operator-1"; productId="product-1"; warehouseId="warehouse-1"; quantity=10; movementType="STOCK_IN" }) | Out-File $log -Append -Encoding utf8

"--- 4. GET /api/inventory (stock consulted by buyer) ---" | Out-File $log -Append -Encoding utf8
"--- 4. GET /api/inventory (stock consulted by buyer) ---" | Out-File $log -Append -Encoding utf8
(Req "GET" "$base/api/inventory?requesterId=buyer-1&productId=product-1&warehouseId=warehouse-1" $null) | Out-File $log -Append -Encoding utf8

"--- 5. POST /api/carts/items (buyer adds 2x product-1) ---" | Out-File $log -Append -Encoding utf8
(Req "POST" "$base/api/carts/items" @{ buyerId="buyer-1"; cartId=""; productId="product-1"; quantity=2; unitPrice=50.00 }) | Out-File $log -Append -Encoding utf8

"--- 6. GET /api/carts ---" | Out-File $log -Append -Encoding utf8
(Req "GET" "$base/api/carts?buyerId=buyer-1" $null) | Out-File $log -Append -Encoding utf8

"--- 7. POST /api/orders (checkout) ---" | Out-File $log -Append -Encoding utf8
$orderJson = Req "POST" "$base/api/orders" @{ buyerId="buyer-1"; cartId="" }
$orderJson | Out-File $log -Append -Encoding utf8
$orderId = ""
try { $orderId = ($orderJson | ConvertFrom-Json).data.orderId } catch {}
"ORDER ID: $orderId" | Out-File $log -Append -Encoding utf8

if ($orderId) {
    "--- 8. POST /api/orders/$orderId/payment (buyer confirms) ---" | Out-File $log -Append -Encoding utf8
    (Req "POST" "$base/api/orders/$orderId/payment" @{ performerId="buyer-1" }) | Out-File $log -Append -Encoding utf8

    "--- 9. POST /api/shipments (operator creates shipment) ---" | Out-File $log -Append -Encoding utf8
    (Req "POST" "$base/api/shipments" @{ operatorId="logistics-operator-1"; orderId=$orderId; originWarehouseId="warehouse-1" }) | Out-File $log -Append -Encoding utf8

    "--- 10. POST /api/shipments/$orderId/dispatch ---" | Out-File $log -Append -Encoding utf8
    (Req "POST" "$base/api/shipments/$orderId/dispatch" @{ operatorId="logistics-operator-1" }) | Out-File $log -Append -Encoding utf8

    "--- 11. POST /api/shipments/$orderId/delivery ---" | Out-File $log -Append -Encoding utf8
    (Req "POST" "$base/api/shipments/$orderId/delivery" @{ operatorId="logistics-operator-1" }) | Out-File $log -Append -Encoding utf8

    "--- 12. GET /api/shipments/$orderId ---" | Out-File $log -Append -Encoding utf8
    (Req "GET" "$base/api/shipments/$orderId?requesterId=buyer-1" $null) | Out-File $log -Append -Encoding utf8

    "--- 13. GET /api/invoices/$orderId ---" | Out-File $log -Append -Encoding utf8
    (Req "GET" "$base/api/invoices/$orderId?requesterId=buyer-1" $null) | Out-File $log -Append -Encoding utf8
}

"--- 14. GET /api/reports/commercial (admin) ---" | Out-File $log -Append -Encoding utf8
(Req "GET" "$base/api/reports/commercial?requesterId=administrator-1" $null) | Out-File $log -Append -Encoding utf8

"--- 15. GET /api/reports/sellers/seller-1 (admin) ---" | Out-File $log -Append -Encoding utf8
(Req "GET" "$base/api/reports/sellers/seller-1?requesterId=administrator-1" $null) | Out-File $log -Append -Encoding utf8

"--- 16. POST /api/authorization/permissions (BUYER vs REGISTER_SELLER -> denied) ---" | Out-File $log -Append -Encoding utf8
(Req "POST" "$base/api/authorization/permissions" @{ userId="buyer-1"; operation="REGISTER_SELLER" }) | Out-File $log -Append -Encoding utf8

"--- 17. POST /api/authorization/ownership (buyer owns its order -> allowed) ---" | Out-File $log -Append -Encoding utf8
(Req "POST" "$base/api/authorization/ownership" @{ userId="buyer-1"; resourceType="ORDER"; resourceId=$orderId }) | Out-File $log -Append -Encoding utf8

"--- 18. POST /api/warehouses (seller registers its warehouse) ---" | Out-File $log -Append -Encoding utf8
(Req "POST" "$base/api/warehouses" @{ performerId="seller-1"; warehouseId="warehouse-2"; name="Seller Storage"; street="Cra 45"; city="Medellin"; state="ANT"; country="Colombia"; postalCode="050001"; ownership="SELLER" }) | Out-File $log -Append -Encoding utf8

"--- 19. GET /api/warehouses/warehouse-2 ---" | Out-File $log -Append -Encoding utf8
(Req "GET" "$base/api/warehouses/warehouse-2?requesterId=administrator-1" $null) | Out-File $log -Append -Encoding utf8

"--- 20. PUT /api/warehouses/warehouse-2 (rename by owner) ---" | Out-File $log -Append -Encoding utf8
(Req "PUT" "$base/api/warehouses/warehouse-2" @{ performerId="seller-1"; newName="Seller Storage II" }) | Out-File $log -Append -Encoding utf8

"--- 21. POST /api/buyers (self-registration) ---" | Out-File $log -Append -Encoding utf8
(Req "POST" "$base/api/buyers" @{ identifier="buyer-2"; fullName="Ana Compradora"; email="ana@nexusmarket.com"; street="Calle 9"; city="Cali"; state="VAC"; country="Colombia"; postalCode="760001" }) | Out-File $log -Append -Encoding utf8

"--- 22. GET /api/buyers/buyer-1/activity (orders+returns) ---" | Out-File $log -Append -Encoding utf8
(Req "GET" "$base/api/buyers/buyer-1/activity?requesterId=administrator-1" $null) | Out-File $log -Append -Encoding utf8

"--- 23. PUT /api/products/product-1 (update description by seller) ---" | Out-File $log -Append -Encoding utf8
(Req "PUT" "$base/api/products/product-1" @{ performerId="seller-1"; newDescription="RGB keyboard pro" }) | Out-File $log -Append -Encoding utf8

"--- 24. POST /api/products/product-1/status (SUSPENDED by seller) ---" | Out-File $log -Append -Encoding utf8
(Req "POST" "$base/api/products/product-1/status" @{ performerId="seller-1"; statusCode="SUSPENDED" }) | Out-File $log -Append -Encoding utf8

"--- 25. GET /api/sellers/seller-1 (admin consults) ---" | Out-File $log -Append -Encoding utf8
(Req "GET" "$base/api/sellers/seller-1?requesterId=administrator-1" $null) | Out-File $log -Append -Encoding utf8

"--- 26. POST /api/sellers/seller-1/status (BLOCKED by admin) ---" | Out-File $log -Append -Encoding utf8
(Req "POST" "$base/api/sellers/seller-1/status" @{ performerId="administrator-1"; sellerId="seller-1"; action="BLOCKED" }) | Out-File $log -Append -Encoding utf8

"=== DONE ===" | Out-File $log -Append -Encoding utf8
