package com.mudotet.ecommerce.order;

import com.mudotet.ecommerce.order.common.dto.BaseResponse;
import com.mudotet.ecommerce.order.controller.OrderController;
import com.mudotet.ecommerce.order.dto.response.OrderResponse;
import com.mudotet.ecommerce.order.service.OrderService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderServiceApplicationTests {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void healthCheck() throws Exception {
        mvc.perform(get("/orders/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order Service is up and running!"));
    }

    @Test
    void getOrderById() throws Exception {
        var id = UUID.randomUUID();
        var response = new OrderResponse();
        response.setId(id);
        when(orderService.getOrderById(id)).thenReturn(BaseResponse.success(response));
        mvc.perform(get("/orders/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    void rejectsInvalidCreateItems() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/orders")
                .contentType("application/json")
                .content("{\"items\":[{\"productVariantId\":\"" + UUID.randomUUID() + "\",\"quantity\":0}]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(orderService);
    }

    @Test
    void rejectsInvalidOrderId() throws Exception {
        mvc.perform(get("/orders/not-a-uuid")).andExpect(status().isBadRequest());
        verifyNoInteractions(orderService);
    }
}
