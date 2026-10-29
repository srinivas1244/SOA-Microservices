package com.example.ecommerce.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final AppProperties properties;
    private final String serverPort;

    public OrderController(AppProperties properties, @Value("${server.port}") String serverPort) {
        this.properties = properties;
        this.serverPort = serverPort;
    }

    @GetMapping("/orders")
    public ServiceInfo orders() {
        return new ServiceInfo("order-service", properties.getMessage(), serverPort,
                properties.getDatabaseUrl(), properties.getEnvironment());
    }

    public record ServiceInfo(String serviceName, String message, String serverPort,
                              String databaseUrl, String environment) {
    }
}
