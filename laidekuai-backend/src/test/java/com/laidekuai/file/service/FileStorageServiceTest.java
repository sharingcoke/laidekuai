package com.laidekuai.file.service;

import com.laidekuai.common.dto.Result;
import com.laidekuai.file.service.impl.FileStorageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void uploadSuccess() throws Exception {
        FileStorageServiceImpl service = new FileStorageServiceImpl();
        setField(service, "uploadPath", tempDir.toString());
        setField(service, "allowedTypes", "jpg,png");
        setField(service, "maxSize", 1024L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "demo.jpg",
                "image/jpeg",
                "test".getBytes(StandardCharsets.UTF_8)
        );

        Result<String> result = service.upload(file);
        assertEquals(0, result.getCode());
        String url = result.getData();
        assertTrue(url.startsWith("/static/files/"));

        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        File saved = new File(tempDir.toFile(), dateDir + File.separator + url.substring(url.lastIndexOf("/") + 1));
        assertTrue(saved.exists());
    }

    @Test
    void uploadRejectsLargeFile() throws Exception {
        FileStorageServiceImpl service = new FileStorageServiceImpl();
        setField(service, "uploadPath", tempDir.toString());
        setField(service, "allowedTypes", "jpg,png");
        setField(service, "maxSize", 3L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "demo.jpg",
                "image/jpeg",
                "1234".getBytes(StandardCharsets.UTF_8)
        );

        Result<String> result = service.upload(file);
        assertEquals(40001, result.getCode());
    }

    @Test
    void uploadRejectsUnsupportedType() throws Exception {
        FileStorageServiceImpl service = new FileStorageServiceImpl();
        setField(service, "uploadPath", tempDir.toString());
        setField(service, "allowedTypes", "jpg,png");
        setField(service, "maxSize", 1024L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "demo.txt",
                "text/plain",
                "test".getBytes(StandardCharsets.UTF_8)
        );

        Result<String> result = service.upload(file);
        assertEquals(40001, result.getCode());
    }

    @Test
    void uploadRejectsEmptyFile() throws Exception {
        FileStorageServiceImpl service = new FileStorageServiceImpl();
        setField(service, "uploadPath", tempDir.toString());
        setField(service, "allowedTypes", "jpg,png");
        setField(service, "maxSize", 1024L);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "demo.jpg",
                "image/jpeg",
                new byte[0]
        );

        Result<String> result = service.upload(file);
        assertEquals(40001, result.getCode());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
