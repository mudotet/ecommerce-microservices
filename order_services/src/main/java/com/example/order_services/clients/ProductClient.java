package com.example.order_services.clients;

import com.example.order_services.dto.response.ProductItemResponse;
import java.util.List;
import java.util.UUID;

public interface ProductClient {
    List<ProductItemResponse> getProductItems(List<UUID> variantIds);
}
