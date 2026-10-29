package com.example.ecommerce.product;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest {

    @Test
    void returnsProductServiceConfiguration() throws Exception {
        AppProperties properties = new AppProperties();
        properties.setEnvironment("local");
        properties.setMessage("Product catalog configuration updated centrally in Git");
        properties.setDatabaseUrl("jdbc:postgresql://localhost:5432/products");
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ProductController(properties, "8081")).build();

        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceName").value("product-service"))
                .andExpect(jsonPath("$.message").value("Product catalog configuration updated centrally in Git"))
                .andExpect(jsonPath("$.serverPort").value("8081"))
                .andExpect(jsonPath("$.databaseUrl").value("jdbc:postgresql://localhost:5432/products"))
                .andExpect(jsonPath("$.environment").value("local"));
    }
}
