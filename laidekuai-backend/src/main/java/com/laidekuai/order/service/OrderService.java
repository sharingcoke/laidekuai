package com.laidekuai.order.service;

import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.order.dto.OrderCreateRequest;
import com.laidekuai.order.dto.OrderDTO;
import com.laidekuai.order.dto.OrderItemDTO;
import com.laidekuai.order.dto.ShipRequest;

import java.util.List;

/**
 * 订单服务接口
 *
 * @author Laidekuai Team
 */
public interface OrderService {

    /**
     * 创建订单（按卖家拆单）
     *
     * @param request 创建请求
     * @param userId  当前用户ID
     * @return 创建的订单列表（按卖家拆分）
     */
    Result<List<OrderDTO>> createOrder(OrderCreateRequest request, Long userId);

    /**
     * 订单支付（模拟）
     *
     * @param orderId 订单ID
     * @param userId  当前用户ID
     * @return 支付结果
     */
    Result<OrderDTO> payOrder(Long orderId, Long userId);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param userId  当前用户ID
     * @param reason  取消原因
     * @return 取消结果
     */
    Result<Void> cancelOrder(Long orderId, Long userId, String reason);

    /**
     * 获取订单详情
     *
     * @param orderId 订单ID
     * @param userId  当前用户ID
     * @return 订单详情
     */
    Result<OrderDTO> getOrderDetail(Long orderId, Long userId);

    /**
     * 买家订单列表
     *
     * @param userId 用户ID
     * @param status 状态筛选（可选）
     * @param page   页码
     * @param size   每页大小
     * @return 订单列表
     */
    Result<PageResult<OrderDTO>> listBuyerOrders(Long userId, String status, Long page, Long size);

    /**
     * 卖家订单列表
     *
     * @param userId 用户ID
     * @param status 状态筛选（可选）
     * @param page   页码
     * @param size   每页大小
     * @return 订单列表
     */
    Result<PageResult<OrderDTO>> listSellerOrders(Long userId, String status, Long page, Long size);

    /**
     * 管理员订单列表
     *
     * @param status   状态筛选（可选）
     * @param buyerId  买家ID（可选）
     * @param sellerId 卖家ID（可选）
     * @param orderNo  订单号（可选）
     * @param startAt  开始时间（可选）
     * @param endAt    结束时间（可选）
     * @param page     页码
     * @param size     每页大小
     * @return 订单列表
     */
    Result<PageResult<OrderDTO>> listAdminOrders(String status,
                                                 Long buyerId,
                                                 Long sellerId,
                                                 String orderNo,
                                                 java.time.LocalDateTime startAt,
                                                 java.time.LocalDateTime endAt,
                                                 Long page,
                                                 Long size);

    /**
     * 卖家发货
     *
     * @param orderId 订单ID
     * @param request 发货请求
     * @param userId  当前用户ID（卖家）
     * @return 发货结果
     */
    Result<OrderDTO> shipOrder(Long orderId, ShipRequest request, Long userId);

    /**
     * 卖家发货（订单项级）
     *
     * @param orderItemId 订单项ID
     * @param request     发货请求
     * @param userId      当前用户ID（卖家）
     * @return 发货结果
     */
    Result<OrderItemDTO> shipOrderItem(Long orderItemId, ShipRequest request, Long userId);

    /**
     * 管理员代发货（订单项级）
     *
     * @param orderItemId 订单项ID
     * @param request     发货请求
     * @param adminId     当前用户ID（管理员）
     * @return 发货结果
     */
    Result<OrderItemDTO> shipOrderItemByAdmin(Long orderItemId, ShipRequest request, Long adminId);

    /**
     * 买家确认收货
     *
     * @param orderId 订单ID
     * @param userId  当前用户ID（买家）
     * @return 确认结果
     */
    Result<OrderDTO> confirmReceive(Long orderId, Long userId);

    /**
     * 买家申请退款（仅未发货订单）
     *
     * @param orderId 订单ID
     * @param userId  当前用户ID（买家）
     * @param reason  退款原因
     * @return 申请结果
     */
    Result<Void> requestRefund(Long orderId, Long userId, String reason);

    /**
     * 卖家处理退款
     *
     * @param orderId  订单ID
     * @param userId   当前用户ID（卖家）
     * @param approved 是否同意
     * @param remark   处理备注
     * @return 处理结果
     */
    Result<Void> handleRefund(Long orderId, Long userId, boolean approved, String remark);
    /**
     * 系统自动取消超时订单
     *
     * @param orderId 订单ID
     * @return 结果
     */
    Result<Void> cancelOrderSystem(Long orderId);
}


