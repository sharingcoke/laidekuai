package com.laidekuai.file.controller;

import com.laidekuai.common.dto.Result;
import lombok.extern.slf4j.Slf4j;
import com.laidekuai.file.service.FileStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 *
 * @author Laidekuai Team
 */
@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * 上传图片
     * POST /api/files/upload
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return fileStorageService.upload(file);
    }
}
