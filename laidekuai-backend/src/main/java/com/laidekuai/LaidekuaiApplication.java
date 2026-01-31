package com.laidekuai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 来得快二手交易平台 - 主应用类
 *
 * @author Laidekuai Team
 * @since 1.0.0
 */
@EnableScheduling
@SpringBootApplication
public class LaidekuaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LaidekuaiApplication.class, args);
    }
}
