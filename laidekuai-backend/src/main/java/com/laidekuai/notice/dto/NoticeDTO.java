package com.laidekuai.notice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告DTO
 *
 * @author Laidekuai Team
 */
@Data
public class NoticeDTO {
    private Long id;
    private String title;
    private String content;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
