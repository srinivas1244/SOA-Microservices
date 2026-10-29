package com.example.ecommerce.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

    private final AppProperties properties;
    private final String serverPort;

    public ProductController(AppProperties properties, @Value("${server.port}") String serverPort) {
        this.properties = properties;
        this.serverPort = serverPort;
    }

    @GetMapping("/products")
    public ServiceInfo products() {
        return new ServiceInfo("product-service", properties.getMessage(), serverPort,
                properties.getDatabaseUrl(), properties.getEnvironment());
    }

    public record ServiceInfo(String serviceName, String message, String serverPort,
                              String databaseUrl, String environment) {
    }
}
