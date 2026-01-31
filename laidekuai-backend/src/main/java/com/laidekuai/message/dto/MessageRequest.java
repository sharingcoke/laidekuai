package com.laidekuai.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 留言请求DTO
 *
 * @author Laidekuai Team
 */
@Data
public class MessageRequest {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    /**
     * 留言内容
     */
    @NotBlank(message = "留言内容不能为空")
    @Size(max = 500, message = "留言内容不能超过500个字符")
    private String content;

    /**
     * 父消息ID（回复时使用）
     */
    private Long parentId;
}
