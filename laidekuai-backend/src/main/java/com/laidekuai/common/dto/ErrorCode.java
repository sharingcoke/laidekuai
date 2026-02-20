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
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),
    CONFLICT(40901, "操作冲突，请刷新后重试"),

    // ========== 用户相关 (401xx) ==========
    USER_NOT_FOUND(40101, "用户不存在"),
    USERNAME_DUPLICATE(40102, "用户名已存在"),
    PASSWORD_ERROR(40103, "密码错误"),
    USER_DISABLED(40104, "用户已被禁用"),
    TOKEN_EXPIRED(40105, "Token已过期"),
    TOKEN_INVALID(40106, "Token无效"),

    // ========== 商品相关 (402xx) ==========
    GOODS_NOT_FOUND(40201, "商品不存在"),
    GOODS_STATUS_ERROR(40202, "商品状态异常"),
    GOODS_STOCK_INSUFFICIENT(40203, "商品库存不足"),
    CATEGORY_NOT_FOUND(40204, "分类不存在"),
    CATEGORY_HAS_GOODS(40205, "分类下有商品，无法删除"),
    CATEGORY_LEVEL_EXCEEDED(40206, "分类层级不能超过3级"),
    CATEGORY_CIRCULAR_REF(40207, "分类存在循环引用"),

    // ========== 订单相关 (403xx) ==========
    ORDER_NOT_FOUND(40301, "订单不存在"),
    ORDER_STATUS_ERROR(40302, "订单状态异常"),
    STOCK_INSUFFICIENT(40303, "库存不足"),
    ACTIVE_ORDERS_EXCEEDED(40304, "活跃订单数已达上限"),
    ORDER_TIMEOUT(40305, "订单已超时"),
    CANNOT_CANCEL_PAID_ORDER(40306, "已支付订单不可取消"),

    // ========== 退款相关 (404xx) ==========
    REFUND_NOT_ALLOWED(40401, "该订单不允许退款"),
    REFUND_ALREADY_APPLIED(40402, "已申请退款，请勿重复操作"),
    DISPUTE_NOT_FOUND(40403, "争议不存在"),
    DISPUTE_ALREADY_RESOLVED(40404, "争议已裁决"),

    // ========== 评价相关 (405xx) ==========
    REVIEW_ALREADY_EXISTS(40501, "已评价，不可重复评价"),
    REVIEW_TIMEOUT(40502, "超过评价时限（30天）"),
    REVIEW_NOT_FOUND(40503, "评价不存在"),

    // ========== 地址相关 (407xx) ==========
    ADDRESS_NOT_FOUND(40701, "地址不存在"),
    ADDRESS_LIMIT_EXCEEDED(40702, "地址数量已达上限"),

    // ========== 文件上传相关 (406xx) ==========
    FILE_TYPE_ERROR(40601, "文件类型不支持"),
    FILE_SIZE_EXCEEDED(40602, "文件大小超过限制"),
    FILE_UPLOAD_FAILED(40603, "文件上传失败");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;
}
