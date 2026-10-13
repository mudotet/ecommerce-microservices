package com.example.order_services;

import com.example.order_services.clients.ProductClient;
import com.example.order_services.clients.impl.ProductClientImpl;
import com.example.order_services.config.ProductClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

class ProductClientConfigurationTests {
    @Test
    void productClientCanStartWithConfiguredWebClientBuilder() {
        new ApplicationContextRunner()
                .withUserConfiguration(ProductClientConfig.class, ProductClientImpl.class)
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertNotNull(context.getBean(ProductClient.class));
                });
    }
}
