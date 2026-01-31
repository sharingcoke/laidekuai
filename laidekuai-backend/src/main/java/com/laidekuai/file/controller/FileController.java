package com.laidekuai.file.controller;

import com.laidekuai.common.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传控制器
 *
 * @author Laidekuai Team
 */
@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Value("${app.upload.allowed-types}")
    private String allowedTypes;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /**
     * 上传图片
     * POST /api/files/upload
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "文件不能为空");
        }

        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase() : "";
        
        if (!allowedTypes.contains(extension)) {
            return Result.error(400, "不支持的文件类型: " + extension);
        }

        try {
            // 生成文件名: yyyyMMdd/uuid.ext
            String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            
            // 确保目录存在
            File destDir = new File(uploadPath, dateDir);
            if (!destDir.exists()) {
                if (!destDir.mkdirs()) {
                    return Result.error(500, "创建上传目录失败");
                }
            }

            // 保存文件
            File destFile = new File(destDir, newFilename);
            file.transferTo(destFile);

            log.info("文件上传成功: {}", destFile.getAbsolutePath());

            // 返回访问URL (相对路径 /files/yyyyMMdd/uuid.ext)
            // 注意：前端访问时需要加上服务器地址，这里返回相对路径
            // 如果context-path是/api，资源映射也是在Servlet容器内吗？
            // WebMvcConfig映射了 /files/**，所以访问路径应该是 /api/files/yyyyMMdd/uuid.ext
            String url = "/files/" + dateDir + "/" + newFilename;
            return Result.success(url);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(500, "文件上传失败");
        }
    }
}
