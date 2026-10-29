package com.example.ecommerce.payment;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentConfigClientIntegrationTest {

    private static final String MESSAGE = "Payment processing configuration loaded from Git";
    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/payments";
    private static final HttpServer CONFIG_SERVER = startConfigServer();

    @Autowired
    private Environment environment;

    @Autowired
    private MockMvc mvc;

    @Test
    void loadsSharedAndPaymentPropertiesFromConfigServerAndServesThem() throws Exception {
        assertThat(environment.getProperty("app.message")).isEqualTo(MESSAGE);
        assertThat(environment.getProperty("app.database-url")).isEqualTo(DATABASE_URL);
        assertThat(environment.getProperty("app.environment")).isEqualTo("local");
        assertThat(environment.getProperty("server.port")).isEqualTo("8083");

        mvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName").value("payment-service"))
                .andExpect(jsonPath("$.message").value(MESSAGE))
                .andExpect(jsonPath("$.serverPort").value("8083"))
                .andExpect(jsonPath("$.databaseUrl").value(DATABASE_URL))
                .andExpect(jsonPath("$.environment").value("local"));
    }

    private static HttpServer startConfigServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 18888), 0);
            server.createContext("/", exchange -> {
                byte[] body = """
                        {"name":"payment-service","profiles":["default"],"label":"main","version":"test","state":null,
                         "propertySources":[
                           {"name":"payment-service.yml","source":{"server.port":8083,"app.message":"%s","app.database-url":"%s"}},
                           {"name":"application.yml","source":{"app.environment":"local","app.owner":"ecommerce-platform"}}
                         ]}
                        """.formatted(MESSAGE, DATABASE_URL).getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                try (var response = exchange.getResponseBody()) {
                    response.write(body);
                }
            });
            server.start();
            return server;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
