package com.example.ecommerce.payment;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerTest {

    @Test
    void returnsPaymentServiceConfiguration() throws Exception {
        AppProperties properties = new AppProperties();
        properties.setEnvironment("local");
        properties.setMessage("Payment processing configuration loaded from Git");
        properties.setDatabaseUrl("jdbc:postgresql://localhost:5432/payments");
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new PaymentController(properties, "8083")).build();

        mvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName").value("payment-service"))
                .andExpect(jsonPath("$.message").value("Payment processing configuration loaded from Git"))
                .andExpect(jsonPath("$.serverPort").value("8083"))
                .andExpect(jsonPath("$.databaseUrl").value("jdbc:postgresql://localhost:5432/payments"))
                .andExpect(jsonPath("$.environment").value("local"));
    }
}
