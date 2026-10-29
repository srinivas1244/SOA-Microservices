package com.example.ecommerce.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    private final AppProperties properties;
    private final String serverPort;

    public PaymentController(AppProperties properties, @Value("${server.port}") String serverPort) {
        this.properties = properties;
        this.serverPort = serverPort;
    }

    @GetMapping("/payments")
    public ServiceInfo payments() {
        return new ServiceInfo("payment-service", properties.getMessage(), serverPort,
                properties.getDatabaseUrl(), properties.getEnvironment());
    }

    public record ServiceInfo(String serviceName, String message, String serverPort,
                              String databaseUrl, String environment) {
    }
}
