package com.mudotet.ecommerce.order.clients;

import com.mudotet.ecommerce.order.dto.response.ProductItemResponse;
import java.util.List;
import java.util.UUID;

public interface ProductClient {
    List<ProductItemResponse> getProductItems(List<UUID> variantIds);
}
