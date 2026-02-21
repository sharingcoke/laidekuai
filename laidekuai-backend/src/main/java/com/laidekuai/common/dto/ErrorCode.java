package com.laidekuai.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 *
 * @author Laidekuai Team
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ========== 通用错误码 ==========
    SUCCESS(0, "ok"),
    VALIDATION_FAILED(40001, "参数校验失败"),
    BAD_REQUEST(40001, "请求参数错误"),
    UNAUTHORIZED(40101, "未授权，请先登录"),
    FORBIDDEN(40301, "无权限访问"),
    NOT_FOUND(40401, "资源不存在"),
    INTERNAL_ERROR(50001, "系统内部错误"),
    CONFLICT(40901, "操作冲突，请刷新后重试"),

    // ========== 用户相关 (401xx) ==========
    USER_NOT_FOUND(40401, "用户不存在"),
    USERNAME_DUPLICATE(40001, "用户名已存在"),
    PASSWORD_ERROR(40001, "密码错误"),
    USER_DISABLED(40301, "用户已被禁用"),
    TOKEN_EXPIRED(40101, "Token已过期"),
    TOKEN_INVALID(40101, "Token无效"),

    // ========== 商品相关 (402xx) ==========
    GOODS_NOT_FOUND(40401, "商品不存在"),
    GOODS_STATUS_ERROR(40901, "商品状态异常"),
    GOODS_STOCK_INSUFFICIENT(40902, "商品库存不足"),
    CATEGORY_NOT_FOUND(40401, "分类不存在"),
    CATEGORY_HAS_GOODS(40901, "分类下有商品，无法删除"),
    CATEGORY_LEVEL_EXCEEDED(40001, "分类层级不能超过3级"),
    CATEGORY_CIRCULAR_REF(40001, "分类存在循环引用"),

    // ========== 订单相关 (403xx) ==========
    ORDER_NOT_FOUND(40401, "订单不存在"),
    ORDER_STATUS_ERROR(40901, "订单状态异常"),
    STOCK_INSUFFICIENT(40902, "库存不足"),
    ACTIVE_ORDERS_EXCEEDED(40903, "活跃订单数已达上限"),
    ORDER_TIMEOUT(40901, "订单已超时"),
    CANNOT_CANCEL_PAID_ORDER(40901, "已支付订单不可取消"),

    // ========== 退款相关 (404xx) ==========
    REFUND_NOT_ALLOWED(40901, "该订单不允许退款"),
    REFUND_ALREADY_APPLIED(40901, "已申请退款，请勿重复操作"),
    DISPUTE_NOT_FOUND(40401, "争议不存在"),
    DISPUTE_ALREADY_RESOLVED(40901, "争议已裁决"),

    // ========== 评价相关 (405xx) ==========
    REVIEW_ALREADY_EXISTS(40901, "已评价，不可重复评价"),
    REVIEW_TIMEOUT(40901, "超过评价时限（30天）"),
    REVIEW_NOT_FOUND(40401, "评价不存在"),

    // ========== 地址相关 (407xx) ==========
    ADDRESS_NOT_FOUND(40401, "地址不存在"),
    ADDRESS_LIMIT_EXCEEDED(40901, "地址数量已达上限"),

    // ========== 文件上传相关 (406xx) ==========
    FILE_TYPE_ERROR(40001, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(40001, "文件大小超过限制"),
    FILE_UPLOAD_FAILED(50001, "文件上传失败");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;
}
