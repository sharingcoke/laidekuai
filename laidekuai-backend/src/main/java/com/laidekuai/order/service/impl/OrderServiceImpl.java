package com.laidekuai.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.laidekuai.address.entity.Address;
import com.laidekuai.address.mapper.AddressMapper;
import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.dto.PageResult;
import com.laidekuai.common.dto.Result;
import com.laidekuai.dispute.entity.Dispute;
import com.laidekuai.dispute.mapper.DisputeMapper;
import com.laidekuai.common.enums.GoodsStatus;
import com.laidekuai.common.util.OrderNoGenerator;
import com.laidekuai.common.util.SecurityUtils;
import com.laidekuai.goods.entity.Goods;
import com.laidekuai.goods.mapper.GoodsMapper;
import com.laidekuai.order.dto.*;
import com.laidekuai.order.entity.Order;
import com.laidekuai.order.entity.OrderItem;
import com.laidekuai.order.mapper.OrderItemMapper;
import com.laidekuai.order.mapper.OrderMapper;
import com.laidekuai.order.service.OrderService;
import com.laidekuai.user.entity.User;
import com.laidekuai.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 璁㈠崟鏈嶅姟瀹炵幇绫?
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
    private final UserMapper userMapper;
    private final DisputeMapper disputeMapper;

    /**
     * 娲昏穬璁㈠崟鏁颁笂闄?
     */
    private static final int MAX_ACTIVE_ORDERS = 10;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<List<OrderDTO>> createOrder(OrderCreateRequest request, Long userId) {
        List<OrderItemRequest> normalizedItems = mergeDuplicateItems(request.getItems());
        log.info("用户 {} 创建订单, 商品数 {}", userId, normalizedItems.size());

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
        List<Long> goodsIds = normalizedItems.stream()
                .map(OrderItemRequest::getGoodsId)
                .collect(Collectors.toList());

        List<Goods> goodsList = goodsMapper.selectBatchIds(goodsIds);
        if (goodsList.size() != goodsIds.size()) {
            return Result.error(ErrorCode.GOODS_NOT_FOUND);
        }

        Map<Long, Goods> goodsMap = goodsList.stream()
                .collect(Collectors.toMap(Goods::getId, g -> g));

        // 校验商品状态和库存
        for (OrderItemRequest item : normalizedItems) {
            Goods goods = goodsMap.get(item.getGoodsId());
            if (goods == null) {
                return Result.error(ErrorCode.GOODS_NOT_FOUND);
            }
            if (goods.getStatus() != GoodsStatus.APPROVED) {
                log.warn("商品 {} 状态异常 {}", goods.getId(), goods.getStatus());
                return Result.error(ErrorCode.GOODS_STATUS_ERROR);
            }
            if (goods.getStock() < item.getQuantity()) {
                log.warn("商品 {} 库存不足, 需要: {}, 实际: {}", goods.getId(), item.getQuantity(), goods.getStock());
                return Result.error(ErrorCode.STOCK_INSUFFICIENT);
            }
            // 检查是否购买自己的商品
            if (goods.getSellerId().equals(userId)) {
                log.warn("用户 {} 不能购买自己的商品 {}", userId, goods.getId());
                return Result.error(ErrorCode.FORBIDDEN.getCode(), "不能购买自己的商品");
            }
        }

        // 4. 按卖家分组
        Map<Long, List<OrderItemRequest>> sellerGroups = normalizedItems.stream()
                .collect(Collectors.groupingBy(item -> goodsMap.get(item.getGoodsId()).getSellerId()));

        List<OrderDTO> createdOrders = new ArrayList<>();

        // 5. 鍒涘缓璁㈠崟锛堟寜鍗栧鎷嗗垎锛?
        for (Map.Entry<Long, List<OrderItemRequest>> entry : sellerGroups.entrySet()) {
            Long sellerId = entry.getKey();
            List<OrderItemRequest> items = entry.getValue();

            // 5.1 鎵ｅ噺搴撳瓨锛堟潯浠禪PDATE闃茶秴鍗栵級
            for (OrderItemRequest item : items) {
                int rows = goodsMapper.deductStock(item.getGoodsId(), item.getQuantity());
                if (rows == 0) {
                    log.error("鍟嗗搧 {} 搴撳瓨鎵ｅ噺澶辫触锛堝彲鑳藉凡琚叾浠栬鍗曟姠鍗狅級", item.getGoodsId());
                    throw new RuntimeException("搴撳瓨鎵ｅ噺澶辫触");
                }
            }

            // 5.2 璁＄畻璁㈠崟鎬婚噾棰?
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderItemRequest item : items) {
                Goods goods = goodsMap.get(item.getGoodsId());
                totalAmount = totalAmount.add(goods.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            // 5.3 鍒涘缓璁㈠崟涓昏〃
            Order order = new Order();
            order.setOrderNo(orderNoGenerator.generate());
            order.setBuyerId(userId);
            order.setSellerId(sellerId);
            order.setTotalAmount(totalAmount);
            order.setShippingFee(BigDecimal.ZERO); // V1 鍏嶈繍璐?
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
            log.info("鍒涘缓璁㈠崟鎴愬姛, 璁㈠崟鍙? {}, 鍗栧: {}, 閲戦: {}", order.getOrderNo(), sellerId, totalAmount);

            // 5.4 鍒涘缓璁㈠崟椤癸紙鍚揩鐓э級
            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderItemRequest item : items) {
                Goods goods = goodsMap.get(item.getGoodsId());

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setGoodsId(goods.getId());
                orderItem.setSellerId(sellerId);
                orderItem.setGoodsTitle(goods.getTitle());
                // 鑾峰彇绗竴寮犲浘鐗囦綔涓哄皝闈?
                orderItem.setGoodsCover(getFirstImage(goods.getImageUrls()));
                orderItem.setPrice(goods.getPrice());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setAmount(goods.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                orderItem.setItemStatus("PENDING_PAY");
                orderItem.setOrderStatus("PENDING_PAY");
                orderItem.setCreatedAt(LocalDateTime.now());

                orderItems.add(orderItem);
            }

            // 鎵归噺鎻掑叆璁㈠崟椤?
            orderItemMapper.batchInsert(orderItems);

            // 杞崲涓篋TO
            createdOrders.add(OrderDTO.fromOrder(order, orderItems));
        }

        log.info("订单创建完成，共拆分为 {} 个订单", createdOrders.size());
        return Result.success(createdOrders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<OrderDTO> payOrder(Long orderId, Long userId) {
        log.info("鐢ㄦ埛 {} 鏀粯璁㈠崟 {}", userId, orderId);

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

        // 鏇存柊璁㈠崟鐘舵€佷负宸叉敮浠?
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 鏇存柊璁㈠崟椤圭姸鎬?
        orderItemMapper.updateStatusByOrderId(orderId, "PAID");

        log.info("璁㈠崟 {} 鏀粯鎴愬姛", order.getOrderNo());

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        return Result.success(OrderDTO.fromOrder(order, items));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelOrder(Long orderId, Long userId, String reason) {
        log.info("鐢ㄦ埛 {} 鍙栨秷璁㈠崟 {}, 鍘熷洜: {}", userId, orderId, reason);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!order.getBuyerId().equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }
        // 鍙湁寰呮敮浠樼姸鎬佸彲浠ュ彇娑?
        if (!"PENDING_PAY".equals(order.getStatus())) {
            return Result.error(ErrorCode.CANNOT_CANCEL_PAID_ORDER);
        }

        // 閲婃斁搴撳瓨
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            goodsMapper.releaseStock(item.getGoodsId(), item.getQuantity());
            log.debug("閲婃斁鍟嗗搧 {} 搴撳瓨: {}", item.getGoodsId(), item.getQuantity());
        }

        // 鏇存柊璁㈠崟鐘舵€?
        order.setStatus("CANCELED");
        order.setCancelReason(reason != null ? reason : "USER");
        order.setCancelTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 鏇存柊璁㈠崟椤圭姸鎬?
        orderItemMapper.updateStatusByOrderId(orderId, "CANCELED");

        log.info("璁㈠崟 {} 鍙栨秷鎴愬姛", order.getOrderNo());
        return Result.success();
    }

    @Override
    public Result<OrderDTO> getOrderDetail(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        // 涔板/鍗栧/绠＄悊鍛樺彲鏌ョ湅
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId) && !SecurityUtils.isAdmin()) {
            return Result.error(ErrorCode.FORBIDDEN);
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        OrderDTO dto = OrderDTO.fromOrder(order, items);
        dto.setBuyerName(resolveUserName(order.getBuyerId()));
        dto.setSellerName(resolveUserName(order.getSellerId()));
        return Result.success(dto);
    }

    private String resolveUserName(Long userId) {
        if (userId == null) {
            return null;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return "用户" + userId;
        }
        if (StringUtils.hasText(user.getNickName())) {
            return user.getNickName();
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername();
        }
        return "用户" + userId;
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
    public Result<PageResult<OrderDTO>> listAdminOrders(String status,
                                                        Long buyerId,
                                                        Long sellerId,
                                                        String orderNo,
                                                        LocalDateTime startAt,
                                                        LocalDateTime endAt,
                                                        Long page,
                                                        Long size) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getDeleted, 0);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        if (buyerId != null) {
            wrapper.eq(Order::getBuyerId, buyerId);
        }
        if (sellerId != null) {
            wrapper.eq(Order::getSellerId, sellerId);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        if (startAt != null) {
            wrapper.ge(Order::getCreatedAt, startAt);
        }
        if (endAt != null) {
            wrapper.le(Order::getCreatedAt, endAt);
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
        log.info("鍗栧 {} 鍙戣揣璁㈠崟 {}", userId, orderId);

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        if (items == null || items.isEmpty()) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }

        for (OrderItem item : items) {
            Result<OrderItemDTO> itemResult = shipOrderItemInternal(item.getId(), request, userId, false);
            if (!itemResult.isSuccess()) {
                return Result.error(itemResult.getCode(), itemResult.getMessage());
            }
        }

        Order updatedOrder = orderMapper.selectById(orderId);
        List<OrderItem> updatedItems = orderItemMapper.selectByOrderId(orderId);
        return Result.success(OrderDTO.fromOrder(updatedOrder, updatedItems));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<OrderItemDTO> shipOrderItem(Long orderItemId, ShipRequest request, Long userId) {
        return shipOrderItemInternal(orderItemId, request, userId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<OrderItemDTO> shipOrderItemByAdmin(Long orderItemId, ShipRequest request, Long adminId) {
        return shipOrderItemInternal(orderItemId, request, adminId, true);
    }

    private Result<OrderItemDTO> shipOrderItemInternal(Long orderItemId, ShipRequest request, Long operatorId, boolean admin) {
        log.info("ship order item {}, operator {}, admin {}", orderItemId, operatorId, admin);

        OrderItem item = orderItemMapper.selectById(orderItemId);
        if (item == null || item.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }

        Order order = orderMapper.selectById(item.getOrderId());
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }

        if (!admin) {
            if (!order.getSellerId().equals(operatorId) || !item.getSellerId().equals(operatorId)) {
                return Result.error(ErrorCode.FORBIDDEN);
            }
        }

        if (!"PAID".equals(order.getStatus())) {
            return Result.error(ErrorCode.ORDER_STATUS_ERROR);
        }
        if (!"PAID".equals(item.getItemStatus())) {
            return Result.error(ErrorCode.ORDER_STATUS_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();
        item.setItemStatus("SHIPPED");
        item.setOrderStatus(order.getStatus());
        item.setShipCompany(request.getShipCompany());
        item.setTrackingNo(request.getTrackingNo());
        item.setShipTime(now);
        item.setUpdatedAt(now);
        orderItemMapper.updateById(item);

        int unshipped = orderItemMapper.countUnshippedItems(order.getId());
        if (unshipped == 0) {
            int updated = orderMapper.markShippedIfPaid(order.getId(), now);
            if (updated > 0) {
                orderItemMapper.updateStatusByOrderId(order.getId(), "SHIPPED");
                item.setOrderStatus("SHIPPED");
            }
        }

        return Result.success(OrderItemDTO.fromOrderItem(item));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<OrderDTO> confirmReceive(Long orderId, Long userId) {
        log.info("涔板 {} 纭鏀惰揣璁㈠崟 {}", userId, orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        // 鍙湁涔板鍙互纭鏀惰揣
        if (!order.getBuyerId().equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }
        // 鍙湁宸插彂璐х姸鎬佸彲浠ョ‘璁ゆ敹璐?
        if (!"SHIPPED".equals(order.getStatus())) {
            return Result.error(ErrorCode.ORDER_STATUS_ERROR);
        }

        // 鏇存柊璁㈠崟鐘舵€佷负宸插畬鎴?
        order.setStatus("COMPLETED");
        order.setIsSettled(1);
        order.setSettledTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        // 鏇存柊璁㈠崟椤圭姸鎬?
        orderItemMapper.updateStatusByOrderId(orderId, "COMPLETED");

        log.info("璁㈠崟 {} 纭鏀惰揣鎴愬姛", order.getOrderNo());

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        return Result.success(OrderDTO.fromOrder(order, items));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> requestRefund(Long orderId, Long userId, String reason) {
        log.info("涔板 {} 鐢宠閫€娆捐鍗?{}, 鍘熷洜: {}", userId, orderId, reason);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        // 鍙湁涔板鍙互鐢宠閫€娆?
        if (!order.getBuyerId().equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }
        // V1 鍙敮鎸佹湭鍙戣揣閫€娆撅紙PAID鐘舵€侊級
        if (!"PAID".equals(order.getStatus())) {
            return Result.error(ErrorCode.REFUND_NOT_ALLOWED);
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        boolean hasShipped = items.stream()
                .anyMatch(item -> !"PAID".equals(item.getItemStatus()));
        if (hasShipped) {
            return Result.error(ErrorCode.REFUND_NOT_ALLOWED);
        }

        // 妫€鏌ラ€€娆炬鏁帮紝杈惧埌2娆″悗绗?娆＄敵璇疯Е鍙戜簤璁?
        int refundCount = order.getRefundRequestCount() != null ? order.getRefundRequestCount() : 0;
        if (refundCount >= 2) {
            Dispute existing = disputeMapper.selectOne(new LambdaQueryWrapper<Dispute>()
                    .eq(Dispute::getOrderId, orderId));
            if (existing == null) {
                Dispute dispute = new Dispute();
                dispute.setOrderId(orderId);
                dispute.setApplicantId(userId);
                dispute.setApplicantType("BUYER");
                dispute.setReason(reason);
                dispute.setStatus("PENDING");
                dispute.setCreatedAt(LocalDateTime.now());
                disputeMapper.insert(dispute);
            }
            order.setStatus("DISPUTED");
            order.setDisputeTime(LocalDateTime.now());
            order.setRefundReason(reason);
            order.setRefundRequestCount(refundCount + 1);
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(order);
            orderItemMapper.updateOrderStatusByOrderId(orderId, "DISPUTED");
            log.info("订单 {} 触发争议，退款申请次数已达 {}", order.getOrderNo(), refundCount + 1);
            return Result.error(ErrorCode.CONFLICT.getCode(), "已触发争议，请等待平台处理");
        }

        // 鏇存柊璁㈠崟鐘舵€佷负閫€娆句腑
        order.setStatus("REFUNDING");
        order.setRefundReason(reason);
        order.setRefundRequestCount(refundCount + 1);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        orderItemMapper.updateOrderStatusByOrderId(orderId, "REFUNDING");

        log.info("订单 {} 退款申请成功，第 {} 次申请", order.getOrderNo(), refundCount + 1);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> handleRefund(Long orderId, Long userId, boolean approved, String remark) {
        log.info("鍗栧 {} 澶勭悊璁㈠崟 {} 閫€娆? 鍚屾剰: {}", userId, orderId, approved);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }
        // 鍙湁鍗栧鍙互澶勭悊閫€娆?
        if (!order.getSellerId().equals(userId)) {
            return Result.error(ErrorCode.FORBIDDEN);
        }
        // 鍙湁閫€娆句腑鐘舵€佸彲浠ュ鐞?
        if (!"REFUNDING".equals(order.getStatus())) {
            return Result.error(ErrorCode.ORDER_STATUS_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();
        UpdateWrapper<Order> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", orderId)
                .eq("deleted", 0)
                .eq("status", "REFUNDING");

        if (approved) {
            wrapper.set("status", "REFUNDED")
                    .set("cancel_reason", "BUYER_REFUND")
                    .set("cancel_time", now)
                    .set("updated_at", now);
        } else {
            wrapper.set("status", "PAID")
                    .set("updated_at", now);
        }

        int updated = orderMapper.update(null, wrapper);
        if (updated == 0) {
            return Result.error(ErrorCode.CONFLICT);
        }

        if (approved) {
            List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
            for (OrderItem item : items) {
                goodsMapper.releaseStock(item.getGoodsId(), item.getQuantity());
                log.debug("释放商品 {} 库存: {}", item.getGoodsId(), item.getQuantity());
            }
            orderItemMapper.updateStatusByOrderId(orderId, "REFUNDED");
            log.info("订单 {} 退款成功", order.getOrderNo());
        } else {
            orderItemMapper.updateOrderStatusByOrderId(orderId, "PAID");
            log.info("订单 {} 退款被拒绝", order.getOrderNo());
        }

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> approveRefundByAdmin(Long orderId, Long adminId) {
        log.info("管理员 {} 审核通过退款，订单 {}", adminId, orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, orderId)
                .eq(Order::getDeleted, 0)
                .eq(Order::getStatus, "REFUNDING")
                .set(Order::getStatus, "REFUNDED")
                .set(Order::getCancelReason, "BUYER_REFUND")
                .set(Order::getCancelTime, now)
                .set(Order::getUpdatedAt, now);

        int updated = orderMapper.update(null, wrapper);
        if (updated == 0) {
            return Result.error(ErrorCode.CONFLICT);
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            goodsMapper.releaseStock(item.getGoodsId(), item.getQuantity());
            log.debug("释放商品 {} 库存: {}", item.getGoodsId(), item.getQuantity());
        }
        orderItemMapper.updateStatusByOrderId(orderId, "REFUNDED");

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> rejectRefundByAdmin(Long orderId, Long adminId, String remark) {
        log.info("管理员 {} 驳回退款，订单 {}, 备注: {}", adminId, orderId, remark);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, orderId)
                .eq(Order::getDeleted, 0)
                .eq(Order::getStatus, "REFUNDING")
                .set(Order::getStatus, "PAID")
                .set(Order::getUpdatedAt, now)
                .setSql("refund_request_count = refund_request_count + 1");

        int updated = orderMapper.update(null, wrapper);
        if (updated == 0) {
            return Result.error(ErrorCode.CONFLICT);
        }

        orderItemMapper.updateOrderStatusByOrderId(orderId, "PAID");
        return Result.success();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelOrderSystem(Long orderId) {
        log.info("系统自动取消超时订单 {}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getDeleted() == 1) {
            return Result.error(ErrorCode.ORDER_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();
        int affected = orderMapper.markCanceledIfPending(orderId, "TIMEOUT", now, now);
        if (affected == 0) {
            return Result.success();
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            goodsMapper.releaseStock(item.getGoodsId(), item.getQuantity());
            log.debug("系统释放商品 {} 库存: {}", item.getGoodsId(), item.getQuantity());
        }

        orderItemMapper.updateStatusByOrderId(orderId, "CANCELED");

        log.info("订单 {} 已因超时被系统取消", order.getOrderNo());
        return Result.success();
    }

    private List<OrderItemRequest> mergeDuplicateItems(List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Integer> merged = new LinkedHashMap<>();
        for (OrderItemRequest item : items) {
            if (item == null || item.getGoodsId() == null || item.getQuantity() == null) {
                continue;
            }
            merged.merge(item.getGoodsId(), item.getQuantity(), Integer::sum);
        }

        List<OrderItemRequest> result = new ArrayList<>(merged.size());
        for (Map.Entry<Long, Integer> entry : merged.entrySet()) {
            OrderItemRequest mergedItem = new OrderItemRequest();
            mergedItem.setGoodsId(entry.getKey());
            mergedItem.setQuantity(entry.getValue());
            result.add(mergedItem);
        }

        return result;
    }

    /**
     * 浠嶫SON鏁扮粍鏍煎紡鐨勫浘鐗嘦RL涓幏鍙栫涓€寮犲浘鐗?
     */
    private String getFirstImage(String imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        // 绠€鍗曡В鏋怞SON鏁扮粍 ["url1","url2"]
        if (imageUrls.startsWith("[") && imageUrls.endsWith("]")) {
            String content = imageUrls.substring(1, imageUrls.length() - 1);
            if (!content.isEmpty()) {
                String[] urls = content.split(",");
                if (urls.length > 0) {
                    String first = urls[0].trim();
                    // 鍘婚櫎寮曞彿
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
