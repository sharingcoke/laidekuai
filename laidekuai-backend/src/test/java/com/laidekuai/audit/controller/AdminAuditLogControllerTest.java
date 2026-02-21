package com.laidekuai.audit.controller;

import com.laidekuai.audit.dto.AuditLogDTO;
import com.laidekuai.audit.service.AuditLogService;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAuditLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void listLogs_ReturnsPage() throws Exception {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(1L);
        dto.setOrderNo("NO100");
        dto.setAction("REFUND_APPROVE");

        PageResult<AuditLogDTO> page = new PageResult<>();
        page.setRecords(List.of(dto));
        page.setTotal(1L);
        page.setCurrent(1L);
        page.setSize(10L);

        when(auditLogService.listAdminLogs(nullable(String.class), nullable(String.class), nullable(String.class), anyLong(), anyLong()))
                .thenReturn(Result.success(page));

        mockMvc.perform(get("/admin/audit-logs")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.records[0].orderNo").value("NO100"));
    }
}
