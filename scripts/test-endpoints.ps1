$ErrorActionPreference = 'Stop'

$services = @(
    @{
        Name = 'product-service'
        Endpoint = 'products'
        Port = '8081'
        Message = 'Product catalog configuration updated centrally in Git'
        DatabaseUrl = 'jdbc:postgresql://localhost:5432/products'
    },
    @{
        Name = 'order-service'
        Endpoint = 'orders'
        Port = '8082'
        Message = 'Order processing configuration loaded from Git'
        DatabaseUrl = 'jdbc:postgresql://localhost:5432/orders'
    },
    @{
        Name = 'payment-service'
        Endpoint = 'payments'
        Port = '8083'
        Message = 'Payment processing configuration loaded from Git'
        DatabaseUrl = 'jdbc:postgresql://localhost:5432/payments'
    }
)

$passed = 0
foreach ($service in $services) {
    $config = Invoke-RestMethod "http://localhost:8888/$($service.Name)/default"
    $resolved = @{}
    foreach ($source in $config.propertySources) {
        foreach ($property in $source.source.PSObject.Properties) {
            if (-not $resolved.ContainsKey($property.Name)) {
                $resolved[$property.Name] = $property.Value
            }
        }
    }

    if ([string]$resolved['server.port'] -ne $service.Port -or
        $resolved['app.message'] -ne $service.Message -or
        $resolved['app.database-url'] -ne $service.DatabaseUrl -or
        $resolved['app.environment'] -ne 'local') {
        throw "Config Server returned unexpected configuration for $($service.Name): $($config | ConvertTo-Json -Depth 10)"
    }
    Write-Host "PASS Config Server: $($service.Name)/default"
    $passed++

    $response = Invoke-RestMethod "http://localhost:$($service.Port)/$($service.Endpoint)"
    if ($response.serviceName -ne $service.Name -or
        $response.message -ne $service.Message -or
        [string]$response.serverPort -ne $service.Port -or
        $response.databaseUrl -ne $service.DatabaseUrl -or
        $response.environment -ne 'local') {
        throw "Service returned unexpected response for $($service.Name): $($response | ConvertTo-Json)"
    }
    Write-Host "PASS REST endpoint: http://localhost:$($service.Port)/$($service.Endpoint)"
    $passed++
}

Write-Host "Passed $passed of 6 end-to-end HTTP checks."
