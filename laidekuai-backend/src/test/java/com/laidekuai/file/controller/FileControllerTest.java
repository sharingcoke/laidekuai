package com.laidekuai.file.controller;

import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.JwtUtil;
import com.laidekuai.file.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void uploadSuccess() throws Exception {
        when(fileStorageService.upload(any())).thenReturn(Result.success("/static/files/20260221/abc.jpg"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "demo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test".getBytes()
        );

        mockMvc.perform(multipart("/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value("/static/files/20260221/abc.jpg"));
    }

    @Test
    void uploadRejectsEmptyFile() throws Exception {
        when(fileStorageService.upload(any())).thenReturn(Result.error(40001, "文件不能为空"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "demo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/files/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.message").value("文件不能为空"));
    }
}
