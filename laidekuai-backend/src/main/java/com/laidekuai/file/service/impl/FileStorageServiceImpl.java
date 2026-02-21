package com.laidekuai.file.service.impl;

import com.laidekuai.common.dto.Result;
import com.laidekuai.file.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Value("${app.upload.allowed-types}")
    private String allowedTypes;

    @Value("${app.upload.max-size}")
    private long maxSize;

    @Override
    public Result<String> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }

        if (file.getSize() > maxSize) {
            return Result.error(400, "文件大小超过限制");
        }

        String extension = getExtension(file.getOriginalFilename());
        if (extension.isEmpty() || !getAllowedTypes().contains(extension)) {
            return Result.error(400, "不支持的文件类型: " + extension);
        }

        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String newFilename = UUID.randomUUID().toString() + "." + extension;

            File destDir = new File(uploadPath, dateDir);
            Files.createDirectories(destDir.toPath());
            File destFile = new File(destDir, newFilename);
            Files.copy(file.getInputStream(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            log.info("文件上传成功: {}", destFile.getAbsolutePath());
            String url = "/files/" + dateDir + "/" + newFilename;
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(500, "文件上传失败");
        }
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int idx = filename.lastIndexOf(".");
        if (idx < 0 || idx == filename.length() - 1) {
            return "";
        }
        return filename.substring(idx + 1).toLowerCase();
    }

    private Set<String> getAllowedTypes() {
        if (allowedTypes == null || allowedTypes.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(allowedTypes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
