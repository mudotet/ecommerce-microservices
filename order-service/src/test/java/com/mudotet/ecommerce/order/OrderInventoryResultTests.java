package com.mudotet.ecommerce.order;

import com.mudotet.ecommerce.order.entity.Order;
import com.mudotet.ecommerce.order.entity.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderInventoryResultTests {
    @Test
    void successfulInventoryResultPreparesNewOrder() {
        var order = new Order();
        order.prepare();
        assertEquals(OrderStatus.PREPARED, order.getStatus());
    }

    @Test
    void failureRejectsOrderAndKeepsReason() {
        var order = new Order();
        order.reject("Insufficient stock");
        assertEquals(OrderStatus.REJECTED, order.getStatus());
        assertEquals("Insufficient stock", order.getFailureReason());
        order.prepare();
        assertEquals(OrderStatus.REJECTED, order.getStatus());
    }

    @Test
    void duplicateSuccessCannotRejectPreparedOrder() {
        var order = new Order();
        order.prepare();
        order.prepare();
        order.reject("Late failure");
        assertEquals(OrderStatus.PREPARED, order.getStatus());
        assertNull(order.getFailureReason());
    }
}
