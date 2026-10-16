package com.mudotet.ecommerce.order;

import com.mudotet.ecommerce.order.clients.ProductClient;
import com.mudotet.ecommerce.order.clients.impl.ProductClientImpl;
import com.mudotet.ecommerce.order.config.ProductClientConfig;
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
