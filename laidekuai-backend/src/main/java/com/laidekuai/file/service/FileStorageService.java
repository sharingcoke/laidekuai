package com.laidekuai.file.service;

import com.laidekuai.common.dto.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务
 */
public interface FileStorageService {

    Result<String> upload(MultipartFile file);
}
