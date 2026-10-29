package com.example.ecommerce.product;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppPropertiesValidationTest {

    @Test
    void reportsMissingRequiredConfiguration() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            var violations = validatorFactory.getValidator().validate(new AppProperties());

            assertThat(violations)
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("environment", "message", "databaseUrl");
        }
    }
}
