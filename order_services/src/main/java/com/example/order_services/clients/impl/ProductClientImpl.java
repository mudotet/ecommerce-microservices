package com.example.order_services.clients.impl;

import com.example.order_services.clients.ProductClient;
import com.example.order_services.dto.response.ProductItemResponse;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProductClientImpl implements ProductClient {
    private final WebClient webClient;

    public ProductClientImpl(WebClient.Builder builder) {
        webClient = builder.defaultHeaders(headers -> headers.setBasicAuth("demo", "demo123")).build();
    }

    @Override
    public List<ProductItemResponse> getProductItems(List<UUID> variantIds) {
        try {
            var result = webClient.post()
                    .uri("http://localhost:8081/products/order-items")
                    .bodyValue(variantIds)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ProductItemResponse>>() {
                    })
                    .block();
            if (result == null || result.size() != variantIds.size()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid product response");
            }
            return result;
        } catch (WebClientResponseException exception) {
            throw new ResponseStatusException(exception.getStatusCode(), "Could not get product information", exception);
        } catch (WebClientRequestException | IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service unavailable", exception);
        }
    }
}
