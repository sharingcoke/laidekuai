package com.laidekuai.review.dto;

import com.laidekuai.review.entity.Review;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 评价响应DTO
 *
 * @author Laidekuai Team
 */
@Data
public class ReviewDTO {

    private Long id;
    private Long orderId;
    private Long orderItemId;
    private Long goodsId;
    private String goodsTitle;
    private Long buyerId;
    private String buyerName; // 买家昵称（若匿名则显示"匿名用户"）
    private Long sellerId;
    private Integer rating;
    private String content;
    private List<String> images;
    private Boolean isAnonymous;
    private String reply;
    private LocalDateTime replyTime;
    private LocalDateTime createdAt;
    private String status;

    /**
     * 从Review实体转换
     */
    public static ReviewDTO fromReview(Review review, String buyerNickname) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setOrderId(review.getOrderId());
        dto.setOrderItemId(review.getOrderItemId());
        dto.setGoodsId(review.getGoodsId());
        dto.setGoodsTitle(null);
        dto.setBuyerId(review.getBuyerId());
        dto.setSellerId(review.getSellerId());
        dto.setRating(review.getRating());
        dto.setContent(review.getContent());
        dto.setIsAnonymous(review.getIsAnonymous() == 1);
        dto.setReply(review.getReply());
        dto.setReplyTime(review.getReplyTime());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setStatus(review.getStatus());

        // 处理匿名
        if (review.getIsAnonymous() == 1) {
            dto.setBuyerName("匿名用户");
        } else {
            dto.setBuyerName(buyerNickname != null ? buyerNickname : "用户" + review.getBuyerId());
        }

        // 解析图片JSON
        dto.setImages(parseImages(review.getImages()));

        return dto;
    }

    private static List<String> parseImages(String imagesJson) {
        List<String> images = new ArrayList<>();
        if (imagesJson == null || imagesJson.isEmpty()) {
            return images;
        }
        // 简单解析JSON数组
        if (imagesJson.startsWith("[") && imagesJson.endsWith("]")) {
            String content = imagesJson.substring(1, imagesJson.length() - 1);
            if (!content.isEmpty()) {
                String[] urls = content.split(",");
                for (String url : urls) {
                    String trimmed = url.trim();
                    if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                        images.add(trimmed.substring(1, trimmed.length() - 1));
                    } else {
                        images.add(trimmed);
                    }
                }
            }
        }
        return images;
    }
}
