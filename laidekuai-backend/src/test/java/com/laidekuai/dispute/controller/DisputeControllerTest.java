package com.laidekuai.dispute.controller;

import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.JwtUtil;
import com.laidekuai.dispute.dto.DisputeDetailDTO;
import com.laidekuai.dispute.service.DisputeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DisputeController.class)
@AutoConfigureMockMvc(addFilters = false)
class DisputeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DisputeService disputeService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void getDisputeDetail_ReturnsOk() throws Exception {
        DisputeDetailDTO dto = new DisputeDetailDTO();
        dto.setId(2L);
        dto.setOrderId(20L);

        when(disputeService.getDisputeDetail(eq(2L), nullable(Long.class), anyBoolean()))
                .thenReturn(Result.success(dto));

        mockMvc.perform(get("/disputes/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.orderId").value(20));
    }
}
