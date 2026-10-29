package com.example.ecommerce.order;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest {

    @Test
    void returnsOrderServiceConfiguration() throws Exception {
        AppProperties properties = new AppProperties();
        properties.setEnvironment("local");
        properties.setMessage("Order processing configuration loaded from Git");
        properties.setDatabaseUrl("jdbc:postgresql://localhost:5432/orders");
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new OrderController(properties, "8082")).build();

        mvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName").value("order-service"))
                .andExpect(jsonPath("$.message").value("Order processing configuration loaded from Git"))
                .andExpect(jsonPath("$.serverPort").value("8082"))
                .andExpect(jsonPath("$.databaseUrl").value("jdbc:postgresql://localhost:5432/orders"))
                .andExpect(jsonPath("$.environment").value("local"));
    }
}
