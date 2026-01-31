package com.laidekuai.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发货请求DTO
 *
 * @author Laidekuai Team
 */
@Data
public class ShipRequest {

    /**
     * 物流公司
     */
    @NotBlank(message = "物流公司不能为空")
    @Size(max = 50, message = "物流公司名称不能超过50个字符")
    private String shipCompany;

    /**
     * 物流单号
     */
    @NotBlank(message = "物流单号不能为空")
    @Size(max = 64, message = "物流单号不能超过64个字符")
    private String trackingNo;
}
