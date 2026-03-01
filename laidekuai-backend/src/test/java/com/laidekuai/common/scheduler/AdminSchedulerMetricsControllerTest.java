package com.laidekuai.common.scheduler;

import com.laidekuai.common.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminSchedulerMetricsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminSchedulerMetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchedulerMetrics schedulerMetrics;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void schedulerMetrics_ReturnsSnapshot() throws Exception {
        when(schedulerMetrics.snapshot()).thenReturn(Map.of(
                "totalRuns", 5,
                "totalScanned", 20,
                "totalCanceled", 7,
                "lastDurationMs", 15
        ));

        mockMvc.perform(get("/admin/system/metrics/scheduler"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.totalRuns").value(5))
                .andExpect(jsonPath("$.data.totalCanceled").value(7));
    }
}
