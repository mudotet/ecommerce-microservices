package com.example.order_services;

import com.example.order_services.clients.impl.ProductClientImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

class ProductClientTests {
    @Test
    void callsProductUrlWithoutAuthenticationAndReadsPrice() {
        var id = UUID.randomUUID();
        var client = new ProductClientImpl(WebClient.builder().exchangeFunction(request -> {
            assertEquals("http://product-services/products/order-items", request.url().toString());
            assertNull(request.headers().getFirst(HttpHeaders.AUTHORIZATION));
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body("[{\"productVariantId\":\"" + id + "\",\"unitPrice\":12.34}]")
                    .build());
        }));

        var result = client.getProductItems(List.of(id));

        assertEquals(id, result.getFirst().getProductVariantId());
        assertEquals(new BigDecimal("12.34"), result.getFirst().getUnitPrice());
    }

    @Test
    void preservesMissingProductStatus() {
        var client = new ProductClientImpl(WebClient.builder().exchangeFunction(request ->
                Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build())));

        var exception = assertThrows(ResponseStatusException.class, () -> client.getProductItems(List.of(UUID.randomUUID())));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
