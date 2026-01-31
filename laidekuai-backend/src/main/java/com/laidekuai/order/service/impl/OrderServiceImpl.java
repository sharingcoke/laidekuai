package com.laidekuai.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.address.entity.Address;
import com.laidekuai.address.mapper.AddressMapper;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.common.util.OrderNoGenerator;
import com.laidekuai.goods.entity.Goods;
import com.laidekuai.goods.mapper.GoodsMapper;
import com.laidekuai.order.dto.*;
import com.laidekuai.order.entity.Order;
import com.laidekuai.order.entity.OrderItem;
import com.laidekuai.order.mapper.OrderItemMapper;
import com.laidekuai.order.mapper.OrderMapper;
import com.laidekuai.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 *
 * @author Laidekuai Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final GoodsMapper goodsMapper;
    private final AddressMapper addressMapper;
    private final OrderNoGenerator orderNoGenerator;

    /**
     * 活跃订单数上限
     */
    private static final int MAX_ACTIVE_ORDERS = 10;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<List<OrderDTO>> createOrder(OrderCreateRequest request, Long userId) {
        log.info("用户 {} 创建订单, 商品数: {}", userId, request.getItems().size());

        // 1. 风控检查：活跃订单数
        int activeOrders = orderMapper.countActiveOrders(userId);
        if (activeOrders >= MAX_ACTIVE_ORDERS) {
            log.warn("用户 {} 活跃订单数已达上限: {}", userId, activeOrders);
            return Result.error(ErrorCode.ACTIVE_ORDERS_EXCEEDED);
        }

        // 2. 校验收货地址
        Address address = addressMapper.selectById(request.getAddressId());
        if (address == null || address.getDeleted() == 1 || !address.getUserId().equals(userId)) {
            return Result.error(ErrorCode.ADDRESS_NOT_FOUND);
        }

        // 3. 查询并校验商品
        List<Long> goodsIds = request.getItems().stream()
                .map(OrderItemRequest::getGoodsId)
                .collect(Collectors.toList());

        List<Goods> goodsList = goodsMapper.selectBatchIds(goodsIds);
        if (goodsList.size() != goodsIds.size()) {
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        Map<Long, Goods> goodsMap = goodsList.stream()
                .collect(Collectors.toMap(Goods::getId, g -> g));

        // 校验商品状态和库存
        for (OrderItemRequest item : request.getItems()) {
            Goods goods = goodsMap.get(item.getGoodsId());
            if (goods == null) {
                return Result.error(ErrorCode.GOODS_NOT_FOUND);
            }
            if (!"APPROVED".equals(goods.getStatus())) {
                log.warn("商品 {} 状态异常: {}", goods.getId(), goods.getStatus());
                return Result.error(ErrorCode.GOODS_STATUS_ERROR);
            }
            if (goods.getStock() < item.getQuantity()) {
                log.warn("商品 {} 库存不足, 需要: {}, 实际: {}", goods.getId(), item.getQuantity(), goods.getStock());
                return Result.error(ErrorCode.STOCK_INSUFFICIENT);
            }
            // 检查是否购买自己的商品
            if (goods.getSellerId().equals(userId)) {
                log.warn("用户 {} 不能购买自己的商品 {}", userId, goods.getId());
                return Result.error(40307, "不能购买自己的商品");
            }
        }

        // 4. 按卖家分组
        Map<Long, List<OrderItemRequest>> sellerGroups = request.getItems().stream()
                .collect(Collectors.groupingBy(item -> goodsMap.get(item.getGoodsId()).getSellerId()));

        List<OrderDTO> createdOrders = new ArrayList<>();

        // 5. 创建订单（按卖家拆分）
        for (Map.Entry<Long, List<OrderItemRequest>> entry : sellerGroups.entrySet()) {
            Long sellerId = entry.getKey();
            List<OrderItemRequest> items = entry.getValue();

            // 5.1 扣减库存（条件UPDATE防超卖）
            for (OrderItemRequest item : items) {
                int rows = goodsMapper.deductStock(item.getGoodsId(), item.getQuantity());
                if (rows == 0) {
                    log.error("商品 {} 库存扣减失败（可能已被其他订单抢占）", item.getGoodsId());
                    throw new RuntimeException("库存扣减失败");
                }
            }

            // 5.2 计算订单总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItemRequest item : items) {
                Goods goods = goodsMap.get(item.getGoodsId());
                totalAmount = totalAmount.add(goods.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            // 5.3 创建订单主表
            Order order = new Order();
            order.setOrderNo(orderNoGenerator.generate());
            order.setBuyerId(userId);
            order.setSellerId(sellerId);
            order.setTotalAmount(totalAmount);
            order.setShippingFee(BigDecimal.ZERO); // V1 免运费
            order.setStatus("PENDING_PAY");
            order.setRemark(request.getRemark());
            order.setAddressId(address.getId());
            order.setReceiverName(address.getReceiverName());
            order.setReceiverPhone(address.getReceiverPhone());
            order.setReceiverAddress(address.getFullAddress());
            order.setIsSettled(0);
            order.setRefundRequestCount(0);
            order.setCreatedAt(LocalDateTime.now());

            orderMapper.insert(order);
            log.info("创建订单成功, 订单号: {}, 卖家: {}, 金额: {}", order.getOrderNo(), sellerId, totalAmount);

            // 5.4 创建订单项（含快照）
            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderItemRequest item : items) {
                Goods goods = goodsMap.get(item.getGoodsId());

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setGoodsId(goods.getId());
                orderItem.setSellerId(sellerId);
                orderItem.setGoodsTitle(goods.getTitle());
                // 获取第一张图片作为封面
                orderItem.setGoodsCover(getFirstImage(goods.getImageUrls()));
                orderItem.setPrice(goods.getPrice());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setAmount(goods.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                orderItem.setItemStatus("PENDING_PAY");
                orderItem.setOrderStatus("PENDING_PAY");
                orderItem.setCreatedAt(LocalDateTime.now());

                orderItems.add(orderItem);
            }

            // 批量插入订单项
            orderItemMapper.batchInsert(orderItems);

            // 转换为DTO
            createdOrders.add(OrderDTO.fromOrder(order, orderItems));
        }

        log.info("订单创建完成, 共拆分为 {} 个订单", createdOrders.size());
        return Result.success(createdOrders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<OrderDTO> payOrder(Long orderId, Long userId) {
        log.info("用户 {} 支付订单 {}", userId, orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getBuyerId().equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }
        if (!"PENDING_PAY".equals(order.getStatus())) {
            return Result.error(ErrorCode.ORDER_STATUS_ERROR);
        }

        // 更新订单状态为已支付
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 更新订单项状态
        orderItemMapper.updateStatusByOrderId(orderId, "PAID");

        log.info("订单 {} 支付成功", order.getOrderNo());

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        return Result.success(OrderDTO.fromOrder(order, items));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelOrder(Long orderId, Long userId, String reason) {
        log.info("用户 {} 取消订单 {}, 原因: {}", userId, orderId, reason);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getBuyerId().equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }
        // 只有待支付状态可以取消
        if (!"PENDING_PAY".equals(order.getStatus())) {
            return Result.error(ErrorCode.CANNOT_CANCEL_PAID_ORDER);
        }

        // 释放库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            goodsMapper.releaseStock(item.getGoodsId(), item.getQuantity());
            log.debug("释放商品 {} 库存: {}", item.getGoodsId(), item.getQuantity());
        }

        // 更新订单状态
        order.setStatus("CANCELED");
        order.setCancelReason(reason != null ? reason : "USER");
        order.setCancelTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 更新订单项状态
        orderItemMapper.updateStatusByOrderId(orderId, "CANCELED");

        log.info("订单 {} 取消成功", order.getOrderNo());
        return Result.success();
    }

    @Override
    public Result<OrderDTO> getOrderDetail(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        // 买家或卖家可查看
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        return Result.success(OrderDTO.fromOrder(order, items));
    }

    @Override
    public Result<PageResult<OrderDTO>> listBuyerOrders(Long userId, String status, Long page, Long size) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getBuyerId, userId)
                .eq(Order::getDeleted, 0);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);

        List<OrderDTO> dtos = result.getRecords().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
                    return OrderDTO.fromOrder(order, items);
                })
                .collect(Collectors.toList());

        PageResult<OrderDTO> pageResult = new PageResult<>();
        pageResult.setRecords(dtos);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());

        return Result.success(pageResult);
    }

    @Override
    public Result<PageResult<OrderDTO>> listSellerOrders(Long userId, String status, Long page, Long size) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getSellerId, userId)
                .eq(Order::getDeleted, 0);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);

        List<OrderDTO> dtos = result.getRecords().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
                    return OrderDTO.fromOrder(order, items);
                })
                .collect(Collectors.toList());

        PageResult<OrderDTO> pageResult = new PageResult<>();
        pageResult.setRecords(dtos);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(result.getCurrent());
        pageResult.setSize(result.getSize());

        return Result.success(pageResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<OrderDTO> shipOrder(Long orderId, ShipRequest request, Long userId) {
        log.info("卖家 {} 发货订单 {}", userId, orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        // 只有卖家可以发货
        if (!order.getSellerId().equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }
        // 只有已支付状态可以发货
        if (!"PAID".equals(order.getStatus())) {
            return Result.error(ErrorCode.ORDER_STATUS_ERROR);
        }

        // 更新订单状态
        order.setStatus("SHIPPED");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 更新订单项状态和物流信息
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            item.setItemStatus("SHIPPED");
            item.setOrderStatus("SHIPPED");
            item.setShipCompany(request.getShipCompany());
            item.setTrackingNo(request.getTrackingNo());
            item.setShipTime(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            orderItemMapper.updateById(item);
        }

        log.info("订单 {} 发货成功, 物流: {} {}", order.getOrderNo(), request.getShipCompany(), request.getTrackingNo());
        return Result.success(OrderDTO.fromOrder(order, items));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<OrderDTO> confirmReceive(Long orderId, Long userId) {
        log.info("买家 {} 确认收货订单 {}", userId, orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        // 只有买家可以确认收货
        if (!order.getBuyerId().equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }
        // 只有已发货状态可以确认收货
        if (!"SHIPPED".equals(order.getStatus())) {
            return Result.error(ErrorCode.ORDER_STATUS_ERROR);
        }

        // 更新订单状态为已完成
        order.setStatus("COMPLETED");
        order.setIsSettled(1);
        order.setSettledTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 更新订单项状态
        orderItemMapper.updateStatusByOrderId(orderId, "COMPLETED");

        log.info("订单 {} 确认收货成功", order.getOrderNo());

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        return Result.success(OrderDTO.fromOrder(order, items));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> requestRefund(Long orderId, Long userId, String reason) {
        log.info("买家 {} 申请退款订单 {}, 原因: {}", userId, orderId, reason);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        // 只有买家可以申请退款
        if (!order.getBuyerId().equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }
        // V1 只支持未发货退款（PAID状态）
        if (!"PAID".equals(order.getStatus())) {
            return Result.error(ErrorCode.REFUND_NOT_ALLOWED);
        }

        // 检查退款次数，达到2次后第3次申请触发争议
        int refundCount = order.getRefundRequestCount() != null ? order.getRefundRequestCount() : 0;
        if (refundCount >= 2) {
            // 触发争议
            order.setStatus("DISPUTED");
            order.setDisputeTime(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(order);
            log.info("订单 {} 触发争议，退款申请次数已达 {}", order.getOrderNo(), refundCount + 1);
            return Result.error(40403, "已触发争议，请等待平台处理");
        }

        // 更新订单状态为退款中
        order.setStatus("REFUNDING");
        order.setRefundRequestCount(refundCount + 1);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单 {} 退款申请成功，第 {} 次申请", order.getOrderNo(), refundCount + 1);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> handleRefund(Long orderId, Long userId, boolean approved, String remark) {
        log.info("卖家 {} 处理订单 {} 退款, 同意: {}", userId, orderId, approved);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        // 只有卖家可以处理退款
        if (!order.getSellerId().equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }
        // 只有退款中状态可以处理
        if (!"REFUNDING".equals(order.getStatus())) {
            return Result.error(ErrorCode.ORDER_STATUS_ERROR);
        }

        if (approved) {
            // 同意退款：释放库存，更新状态
            List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
            for (OrderItem item : items) {
                goodsMapper.releaseStock(item.getGoodsId(), item.getQuantity());
                log.debug("释放商品 {} 库存: {}", item.getGoodsId(), item.getQuantity());
            }

            order.setStatus("REFUNDED");
            order.setCancelReason("BUYER_REFUND");
            order.setCancelTime(LocalDateTime.now());
            orderItemMapper.updateStatusByOrderId(orderId, "CANCELED");
            log.info("订单 {} 退款成功", order.getOrderNo());
        } else {
            // 拒绝退款：恢复为已支付状态
            order.setStatus("PAID");
            log.info("订单 {} 退款被拒绝", order.getOrderNo());
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        return Result.success();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelOrderSystem(Long orderId) {
        log.info("系统系统自动取消超时订单 {}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        // 只有待支付状态可以取消
        if (!"PENDING_PAY".equals(order.getStatus())) {
            return Result.success(); // 状态已变更，不算失败
        }

        // 释放库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            goodsMapper.releaseStock(item.getGoodsId(), item.getQuantity());
            log.debug("系统释放商品 {} 库存: {}", item.getGoodsId(), item.getQuantity());
            
            // 更新订单项状态
            item.setItemStatus("CANCELED");
            item.setOrderStatus("CANCELED");
            item.setUpdatedAt(LocalDateTime.now());
            orderItemMapper.updateById(item);
        }

        // 更新订单状态
        order.setStatus("CANCELED");
        order.setCancelReason("TIMEOUT_CANCELED");
        order.setCancelTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单 {} 已因超时被系统取消", order.getOrderNo());
        return Result.success();
    }

    /**
     * 从JSON数组格式的图片URL中获取第一张图片
     */
    private String getFirstImage(String imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        // 简单解析JSON数组 ["url1","url2"]
        if (imageUrls.startsWith("[") && imageUrls.endsWith("]")) {
            String content = imageUrls.substring(1, imageUrls.length() - 1);
            if (!content.isEmpty()) {
                String[] urls = content.split(",");
                if (urls.length > 0) {
                    String first = urls[0].trim();
                    // 去除引号
                    if (first.startsWith("\"") && first.endsWith("\"")) {
                        return first.substring(1, first.length() - 1);
                    }
                    return first;
                }
            }
        }
        return imageUrls;
    }
}

