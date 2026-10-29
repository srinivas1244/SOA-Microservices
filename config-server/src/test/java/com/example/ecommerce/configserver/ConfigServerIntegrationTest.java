package com.example.ecommerce.configserver;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConfigServerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void returnsSharedAndServiceSpecificConfigurationForEachService() {
        assertConfiguration("product-service", "Product catalog configuration updated centrally in Git",
                "jdbc:postgresql://localhost:5432/products", 8081);
        assertConfiguration("order-service", "Order processing configuration loaded from Git",
                "jdbc:postgresql://localhost:5432/orders", 8082);
        assertConfiguration("payment-service", "Payment processing configuration loaded from Git",
                "jdbc:postgresql://localhost:5432/payments", 8083);
    }

    @Test
    void returnsSharedConfigurationButNoServiceMessageForAnUnknownApplication() {
        ResponseEntity<JsonNode> response =
                restTemplate.getForEntity("/not-a-service/default", JsonNode.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode environment = response.getBody();
        assertThat(environment).isNotNull();
        assertThat(property(environment, "app.environment")).isEqualTo("local");
        assertThat(propertySources(environment).stream()
                .noneMatch(source -> !source.path("source").path("app.message").isMissingNode()))
                .isTrue();
    }

    private void assertConfiguration(String application, String message, String databaseUrl, int port) {
        ResponseEntity<JsonNode> response =
                restTemplate.getForEntity("/{application}/default", JsonNode.class, application);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode environment = response.getBody();
        assertThat(environment).isNotNull();
        assertThat(property(environment, "app.message")).isEqualTo(message);
        assertThat(property(environment, "app.database-url")).isEqualTo(databaseUrl);
        assertThat(property(environment, "app.environment")).isEqualTo("local");
        assertThat(property(environment, "server.port")).isEqualTo(Integer.toString(port));
    }

    private String property(JsonNode environment, String name) {
        return propertySources(environment).stream()
                .map(propertySource -> propertySource.path("source").path(name))
                .filter(value -> !value.isMissingNode())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing configuration property: " + name))
                .asText();
    }

    private java.util.List<JsonNode> propertySources(JsonNode environment) {
        return StreamSupport.stream(environment.path("propertySources").spliterator(), false).toList();
    }
}
