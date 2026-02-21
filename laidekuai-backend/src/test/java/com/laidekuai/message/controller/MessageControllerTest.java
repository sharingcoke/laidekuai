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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void listGoodsMessages_NewAndCompatRoutes_Consistent() throws Exception {
        MessageDTO dto = new MessageDTO();
        dto.setId(1L);
        dto.setGoodsId(2L);
        dto.setContent("hello");

        PageResult<MessageDTO> page = new PageResult<>();
        page.setRecords(List.of(dto));
        page.setTotal(1L);
        page.setCurrent(1L);
        page.setSize(10L);
        page.setPages(1L);

        when(messageService.listGoodsMessages(eq(2L), eq(1L), eq(10L), isNull()))
                .thenReturn(Result.success(page));

        MvcResult newRoute = mockMvc.perform(get("/goods/2/messages")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        MvcResult compatRoute = mockMvc.perform(get("/messages/goods/2")
                .param("page", "1")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        String newBody = newRoute.getResponse().getContentAsString();
        String compatBody = compatRoute.getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertEquals(newBody, compatBody);
    }

    @Test
    void replyMessage_AcceptsJsonBody() throws Exception {
        MessageDTO dto = new MessageDTO();
        dto.setId(11L);

        when(messageService.replyMessage(eq(11L), eq("ok"), any()))
                .thenReturn(Result.success(dto));

        mockMvc.perform(post("/messages/11/replies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"ok\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
