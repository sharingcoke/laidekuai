package com.laidekuai.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC配置
 *
 * @author Laidekuai Team
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保路径以/结尾
        String path = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
        
        // 映射 /static/files/** 到本地上传目录
        registry.addResourceHandler("/static/files/**")
                .addResourceLocations("file:" + new File(path).getAbsolutePath() + "/");
    }
}
