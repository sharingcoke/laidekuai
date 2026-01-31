package com.laidekuai.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 地址创建/更新请求DTO
 *
 * @author Laidekuai Team
 */
@Data
public class AddressRequest {

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
    @Size(max = 50, message = "收货人姓名不能超过50个字符")
    private String receiverName;

    /**
     * 收货人电话
     */
    @NotBlank(message = "收货人电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String receiverPhone;

    /**
     * 省
     */
    @Size(max = 50, message = "省份名称不能超过50个字符")
    private String province;

    /**
     * 市
     */
    @Size(max = 50, message = "城市名称不能超过50个字符")
    private String city;

    /**
     * 区
     */
    @Size(max = 50, message = "区县名称不能超过50个字符")
    private String district;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址不能超过255个字符")
    private String detail;

    /**
     * 是否设为默认地址
     */
    private Boolean isDefault;
}
