package com.mudotet.ecommerce.order;

import com.mudotet.ecommerce.order.clients.ProductClient;
import com.mudotet.ecommerce.order.dto.request.AddOrderRequest;
import com.mudotet.ecommerce.order.dto.request.OrderItemRequest;
import com.mudotet.ecommerce.order.dto.response.ProductItemResponse;
import java.util.List;
import com.mudotet.ecommerce.order.entity.Order;
import com.mudotet.ecommerce.order.entity.OrderItem;
import com.mudotet.ecommerce.order.mapper.OrderMapper;
import com.mudotet.ecommerce.order.repository.OrderRepository;
import com.mudotet.ecommerce.order.service.impl.OrderServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import com.mudotet.ecommerce.order.event.OrderCreatedEvent;
import com.mudotet.ecommerce.order.entity.OrderStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTests {
    private final OrderRepository repository = mock(OrderRepository.class);
    private final ProductClient client = mock(ProductClient.class);
    private final KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
    private final OrderServiceImpl service = new OrderServiceImpl(
            repository, Mappers.getMapper(OrderMapper.class),
            client, kafkaTemplate);

    @Test
    void mapsItemsWithoutReturningOrderBackReference() {
        var orderId = UUID.randomUUID();
        var variantId = UUID.randomUUID();
        var order = new Order();
        var item = new OrderItem();
        item.setProductVariantId(variantId);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("12.34"));
        order.addItem(item);
        when(repository.findById(orderId)).thenReturn(Optional.of(order));

        var response = service.getOrderById(orderId).getData();
        assertSame(order, item.getOrder());
        assertEquals(1, response.getItems().size());
        assertEquals(variantId, response.getItems().getFirst().getProductVariantId());
        assertEquals(2, response.getItems().getFirst().getQuantity());
        assertEquals(new BigDecimal("12.34"), response.getItems().getFirst().getUnitPrice());
    }

    @Test
    void createsOrderUsingProductPrice() {
        var request = request();
        var variant = request.getItems().getFirst().getProductVariantId();
        when(client.getProductItems(List.of(request.getItems().getFirst().getProductVariantId()))).thenReturn(List.of(
                new ProductItemResponse(variant, new BigDecimal("12.34"))));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });
        var response = service.createOrder(request).getData();
        assertEquals(OrderStatus.NEW, response.getStatus());
        assertEquals(new BigDecimal("24.68"), response.getTotalAmount());
        assertEquals(variant, response.getItems().getFirst().getProductVariantId());
        assertEquals(new BigDecimal("12.34"), response.getItems().getFirst().getUnitPrice());
        assertEquals(2, response.getItems().getFirst().getQuantity());
        verify(client).getProductItems(List.of(request.getItems().getFirst().getProductVariantId()));
        var event = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(kafkaTemplate).send(eq("order_created"), event.capture());
        assertEquals(response.getId(), event.getValue().getOrderId());
        assertEquals(variant, event.getValue().getItems().getFirst().getProductVariantId());
        assertEquals(2, event.getValue().getItems().getFirst().getQuantity());
    }

    @Test
    void productFailureDoesNotSaveOrder() {
        var request = request();
        when(client.getProductItems(anyList())).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Product variant not found"));

        assertThrows(ResponseStatusException.class, () -> service.createOrder(request));

        verifyNoInteractions(repository, kafkaTemplate);
    }

    @Test
    void matchesRequestQuantitiesWhenProductResponseOrderDiffers() {
        var request = request();
        var first = request.getItems().getFirst();
        var second = new OrderItemRequest();
        second.setProductVariantId(UUID.randomUUID());
        second.setQuantity(5);
        request.setItems(List.of(first, second));
        when(client.getProductItems(List.of(first.getProductVariantId(), second.getProductVariantId())))
                .thenReturn(List.of(new ProductItemResponse(second.getProductVariantId(), BigDecimal.TEN),
                        new ProductItemResponse(first.getProductVariantId(), BigDecimal.ONE)));
        when(repository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });

        var response = service.createOrder(request).getData();

        assertEquals(second.getProductVariantId(), response.getItems().getFirst().getProductVariantId());
        assertEquals(5, response.getItems().getFirst().getQuantity());
        assertEquals(2, response.getItems().get(1).getQuantity());
    }

    @Test
    void propagatesSaveFailure() {
        var request = request();
        when(client.getProductItems(List.of(request.getItems().getFirst().getProductVariantId()))).thenReturn(List.of(
                new ProductItemResponse(
                        request.getItems().getFirst().getProductVariantId(), BigDecimal.ONE)));
        when(repository.save(any(Order.class))).thenThrow(new IllegalStateException("Database unavailable"));
        assertThrows(IllegalStateException.class, () -> service.createOrder(request));
    }

    @Test
    void rejectsMismatchedProductResponse() {
        var request = request();
        when(client.getProductItems(List.of(request.getItems().getFirst().getProductVariantId()))).thenReturn(List.of(
                new ProductItemResponse(UUID.randomUUID(), BigDecimal.ONE)));
        assertThrows(ResponseStatusException.class, () -> service.createOrder(request));
        verifyNoInteractions(repository, kafkaTemplate);
    }

    @Test
    void rejectsDuplicatesBeforeCallingProduct() {
        var request = request();
        request.setItems(List.of(request.getItems().getFirst(), request.getItems().getFirst()));
        assertThrows(ResponseStatusException.class, () -> service.createOrder(request));
        verifyNoInteractions(client, repository);
    }

    private AddOrderRequest request() {
        var item = new OrderItemRequest();
        item.setProductVariantId(UUID.randomUUID());
        item.setQuantity(2);
        var request = new AddOrderRequest();
        request.setItems(List.of(item));
        return request;
    }

    @Test
    void missingOrderReturnsNotFound() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        var exception = assertThrows(ResponseStatusException.class, () -> service.getOrderById(id));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
