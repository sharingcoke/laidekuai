package com.laidekuai.dispute.controller;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.JwtUtil;
import com.laidekuai.dispute.dto.DisputeDTO;
import com.laidekuai.dispute.dto.DisputeResolveRequest;
import com.laidekuai.dispute.service.DisputeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminDisputeController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminDisputeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DisputeService disputeService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void listAdminDisputes_ReturnsPage() throws Exception {
        DisputeDTO dto = new DisputeDTO();
        dto.setId(1L);
        dto.setOrderId(10L);
        dto.setApplicantName("张三");

        PageResult<DisputeDTO> page = new PageResult<>();
        page.setRecords(List.of(dto));
        page.setTotal(1L);
        page.setCurrent(1L);
        page.setSize(10L);

        when(disputeService.listAdminDisputes(any(), any(), nullable(Long.class), nullable(Long.class)))
                .thenReturn(Result.success(page));

        mockMvc.perform(get("/admin/disputes")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.records[0].applicantName").value("张三"));
    }

    @Test
    void resolveDispute_ReturnsOk() throws Exception {
        when(disputeService.resolveDispute(eq(1L), eq("REFUND"), eq("同意"), nullable(Long.class)))
                .thenReturn(Result.success());

        String body = "{\"resolution\":\"REFUND\",\"adminNote\":\"同意\"}";
        mockMvc.perform(post("/admin/disputes/1/resolve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
