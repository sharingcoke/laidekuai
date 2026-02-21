package com.laidekuai.review.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 卖家回复评价请求DTO
 *
 * @author Laidekuai Team
 */
@Data
public class ReviewReplyRequest {

    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @JsonProperty("sellerReply")
    @JsonAlias({"seller_reply", "content", "reply"})
    private String sellerReply;
}
