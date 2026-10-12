package com.example.order_services;

import com.example.order_services.common.dto.BaseResponse;
import com.example.order_services.controller.OrderController;
import com.example.order_services.dto.response.OrderResponse;
import com.example.order_services.service.OrderServices;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderServicesApplicationTests {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private OrderServices orderServices;

    @Test
    @WithMockUser
    void healthCheck() throws Exception {
        mvc.perform(get("/orders/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order Service is up and running!"));
    }

    @Test
    @WithMockUser
    void getOrderById() throws Exception {
        var id = UUID.randomUUID();
        var response = new OrderResponse();
        response.setId(id);
        when(orderServices.getOrderById(id)).thenReturn(BaseResponse.success(response));
        mvc.perform(get("/orders/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    @WithMockUser
    void rejectsInvalidCreateItems() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/orders")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType("application/json")
                .content("{\"items\":[{\"productVariantId\":\"" + UUID.randomUUID() + "\",\"quantity\":0}]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(orderServices);
    }

    @Test
    @WithMockUser
    void rejectsInvalidOrderId() throws Exception {
        mvc.perform(get("/orders/not-a-uuid")).andExpect(status().isBadRequest());
        verifyNoInteractions(orderServices);
    }
}
