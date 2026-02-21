package com.laidekuai.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 争议裁决请求
 */
@Data
public class DisputeResolveRequest {

    @NotBlank(message = "resolution不能为空")
    private String resolution;

    private String adminNote;
}
