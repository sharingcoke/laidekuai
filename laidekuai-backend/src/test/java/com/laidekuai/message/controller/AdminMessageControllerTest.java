package com.laidekuai.message.controller;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.JwtUtil;
import com.laidekuai.message.dto.MessageDTO;
import com.laidekuai.message.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminMessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void listAdminMessages_ReturnsData() throws Exception {
        PageResult<MessageDTO> page = new PageResult<>();
        page.setRecords(List.of(new MessageDTO()));
        page.setTotal(1L);
        page.setCurrent(1L);
        page.setSize(10L);
        page.setPages(1L);

        when(messageService.listAdminMessages(eq("visible"), eq(1L), eq(10L)))
                .thenReturn(Result.success(page));

        mockMvc.perform(get("/admin/messages")
                .param("status", "visible")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void hideMessage_ReturnsSuccess() throws Exception {
        when(messageService.hideMessage(eq(1L), any())).thenReturn(Result.success());

        mockMvc.perform(post("/admin/messages/1/hide"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
