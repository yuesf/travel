package com.travel.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.ResultCode;
import com.travel.dto.OrderCreateRequest;
import com.travel.dto.OrderStatisticsResponse;
import com.travel.dto.PageResult;
import com.travel.entity.*;
import com.travel.exception.BusinessException;
import com.travel.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private AttractionMapper attractionMapper;
    
    @Autowired
    private HotelRoomMapper hotelRoomMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private CouponMapper couponMapper;
    
    @Autowired
    private UserCouponMapper userCouponMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 获取用户订单列表（按状态分类）
     */
    public PageResult<Order> getUserOrders(Long userId, String status, Integer page, Integer pageSize) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        
        // 参数校验
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        // 计算偏移量
        int offset = (page - 1) * pageSize;
        
        // 查询列表
        List<Order> list = orderMapper.selectByUserId(userId, status, offset, pageSize);
        
        // 批量查询订单项（避免 N+1 查询）
        if (!list.isEmpty()) {
            List<Long> orderIds = list.stream()
                .map(Order::getId)
                .collect(Collectors.toList());
            
            // 批量查询订单项
            List<OrderItem> allItems = orderItemMapper.selectByOrderIds(orderIds);
            
            // 按订单ID分组
            Map<Long, List<OrderItem>> itemsMap = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));
            
            // 填充订单项商品信息
            enrichOrderItems(allItems);
            
            // 为每个订单设置 items 字段
            for (Order order : list) {
                List<OrderItem> items = itemsMap.getOrDefault(order.getId(), Collections.emptyList());
                order.setItems(items);
            }
        }
        
        // 查询总数
        long total = orderMapper.countByUserId(userId, status);
        
        return new PageResult<>(list, total, page, pageSize);
    }
    
    /**
     * 获取用户订单统计数量
     */
    public OrderStatisticsResponse getOrderStatistics(Long userId) {
        // Controller 层已经检查了 userId，这里不需要再次检查
        // 如果 userId 为 null，说明认证失败，应该在 Controller 层返回 UNAUTHORIZED
        
        OrderStatisticsResponse response = new OrderStatisticsResponse();
        
        // 统计各状态订单数量
        response.setPendingPay(orderMapper.countByUserId(userId, "PENDING_PAY"));
        response.setPendingUse(orderMapper.countByUserId(userId, "PAID"));
        response.setCompleted(orderMapper.countByUserId(userId, "COMPLETED"));
        response.setCancelled(orderMapper.countByUserId(userId, "CANCELLED"));
        
        return response;
    }
    
    /**
     * 获取订单详情
     */
    public Order getOrderDetail(Long orderId, Long userId) {
        if (orderId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单ID不能为空");
        }
        
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "订单不存在");
        }
        
        // 验证订单是否属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权访问该订单");
        }
        
        // 查询订单项列表
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        
        // 填充订单项商品信息
        enrichOrderItems(items);
        
        // 设置订单项列表
        order.setItems(items);
        
        return order;
    }
    
    /**
     * 填充订单项商品信息（图片、编码）
     * @param items 订单项列表
     */
    private void enrichOrderItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        
        // 提取商品ID列表（itemType = 'PRODUCT'）
        List<Long> productIds = items.stream()
            .filter(item -> "PRODUCT".equals(item.getItemType()))
            .filter(item -> item.getItemId() != null)
            .map(OrderItem::getItemId)
            .distinct()
            .collect(Collectors.toList());
        
        if (productIds.isEmpty()) {
            return;
        }
        
        // 批量查询商品信息
        List<Product> products = productMapper.selectByIds(productIds);
        
        // 构建商品ID到商品的映射
        Map<Long, Product> productMap = products.stream()
            .collect(Collectors.toMap(Product::getId, product -> product));
        
        // 填充订单项商品信息
        for (OrderItem item : items) {
            if (!"PRODUCT".equals(item.getItemType())) {
                continue;
            }
            
            Product product = productMap.get(item.getItemId());
            if (product != null) {
                // 如果订单项没有保存图片，从商品获取
                if (item.getItemImage() == null || item.getItemImage().isEmpty()) {
                    // 获取商品第一张图片
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        item.setItemImage(product.getImages().get(0));
                    }
                }
                
                // 如果订单项没有保存编码，从商品获取
                if (item.getItemCode() == null || item.getItemCode().isEmpty()) {
                    item.setItemCode(product.getCode());
                }
            }
            // 如果商品不存在或已删除，使用订单项保存的 itemName 作为降级方案
            // itemName 已经在创建订单时保存，无需额外处理
        }
    }
    
    /**
     * 取消订单
     */
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        if (orderId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单ID不能为空");
        }
        
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "订单不存在");
        }
        
        // 验证订单是否属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作该订单");
        }
        
        // 只能取消未支付的订单
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "只能取消未支付的订单");
        }
        
        // 更新订单状态
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);
        
        log.info("订单已取消，订单ID: {}, 用户ID: {}", orderId, userId);
    }
    
    /**
     * 申请退款
     */
    @Transactional
    public void applyRefund(Long orderId, Long userId, String reason) {
        if (orderId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单ID不能为空");
        }
        
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "订单不存在");
        }
        
        // 验证订单是否属于当前用户
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作该订单");
        }
        
        // 验证订单状态：只有已支付或已使用的订单可以申请退款
        String status = order.getStatus();
        if (!"PAID".equals(status) && !"USED".equals(status)) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), 
                "只有已支付或已使用的订单可以申请退款");
        }
        
        // 更新订单状态为退款中（这里简化为直接退款，实际应该有退款审核流程）
        order.setStatus("REFUNDED");
        orderMapper.updateById(order);
        
        log.info("订单已申请退款，订单ID: {}, 用户ID: {}, 原因: {}", orderId, userId, reason);
    }
    
    /**
     * 创建订单（从购物车结算或直接购买）
     */
    @Transactional
    public Order createOrder(Long userId, OrderCreateRequest request) {
        if (userId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户ID不能为空");
        }
        
        // 1. 获取订单项列表
        List<OrderItem> orderItems = new ArrayList<>();
        if (request.getCartIds() != null && !request.getCartIds().isEmpty()) {
            // 从购物车结算
            orderItems = getOrderItemsFromCart(userId, request.getCartIds());
        } else if (request.getItems() != null && !request.getItems().isEmpty()) {
            // 直接购买
            orderItems = getOrderItemsFromRequest(userId, request.getItems());
        } else {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单项不能为空");
        }
        
        if (orderItems.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "订单项不能为空");
        }
        
        // 2. 验证库存
        validateStock(orderItems);
        
        // 3. 计算订单金额
        BigDecimal totalAmount = calculateTotalAmount(orderItems);
        
        // 4. 验证和使用优惠券
        BigDecimal discountAmount = BigDecimal.ZERO;
        Long couponId = null;
        UserCoupon userCoupon = null;
        if (request.getCouponId() != null) {
            userCoupon = validateAndUseCoupon(userId, request.getCouponId(), totalAmount, orderItems);
            if (userCoupon != null && userCoupon.getCoupon() != null) {
                couponId = userCoupon.getCouponId();
                discountAmount = calculateCouponDiscount(userCoupon.getCoupon(), totalAmount);
            }
        }
        
        // 5. 计算实付金额
        BigDecimal payAmount = totalAmount.subtract(discountAmount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }
        
        // 6. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setOrderType(request.getOrderType());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(payAmount);
        order.setCouponId(couponId);
        order.setStatus("PENDING_PAY");
        order.setContactName(request.getContactName());
        order.setContactPhone(request.getContactPhone());
        order.setRemark(request.getRemark());
        
        int result = orderMapper.insert(order);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "订单创建失败");
        }
        
        // 7. 创建订单明细
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
        }
        orderItemMapper.insertBatch(orderItems);
        
        // 8. 更新库存
        updateStock(orderItems);
        
        // 9. 如果是首次下单，更新用户状态
        User user = userMapper.selectById(userId);
        if (user != null && user.getIsFirstOrder() != null && user.getIsFirstOrder() == 1) {
            userMapper.updateFirstOrderStatus(userId, 0);
        }
        
        // 10. 如果使用了优惠券，更新优惠券状态（支付成功后才会真正使用，这里先标记）
        // 注意：优惠券的实际使用在支付成功后处理
        
        // 11. 如果是从购物车结算，删除购物车
        if (request.getCartIds() != null && !request.getCartIds().isEmpty()) {
            for (Long cartId : request.getCartIds()) {
                cartMapper.deleteById(cartId);
            }
        }
        
        log.info("订单创建成功，订单ID: {}, 订单号: {}, 用户ID: {}", order.getId(), order.getOrderNo(), userId);
        
        return order;
    }
    
    /**
     * 从购物车获取订单项
     */
    private List<OrderItem> getOrderItemsFromCart(Long userId, List<Long> cartIds) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (Long cartId : cartIds) {
            Cart cart = cartMapper.selectById(cartId);
            if (cart == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "购物车不存在，ID: " + cartId);
            }
            
            if (!cart.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作该购物车");
            }
            
            OrderItem item = createOrderItem(cart.getItemType(), cart.getItemId(), cart.getQuantity(), null, null, null);
            orderItems.add(item);
        }
        
        return orderItems;
    }
    
    /**
     * 从请求获取订单项
     */
    private List<OrderItem> getOrderItemsFromRequest(Long userId, List<OrderCreateRequest.OrderItemRequest> itemRequests) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderCreateRequest.OrderItemRequest itemRequest : itemRequests) {
            OrderItem item = createOrderItem(
                itemRequest.getItemType(),
                itemRequest.getItemId(),
                itemRequest.getQuantity(),
                itemRequest.getCheckInDate(),
                itemRequest.getCheckOutDate(),
                itemRequest.getUseDate()
            );
            orderItems.add(item);
        }
        
        return orderItems;
    }
    
    /**
     * 创建订单项
     */
    private OrderItem createOrderItem(String itemType, Long itemId, Integer quantity,
                                     LocalDate checkInDate, LocalDate checkOutDate, LocalDate useDate) {
        OrderItem item = new OrderItem();
        item.setItemType(itemType);
        item.setItemId(itemId);
        item.setQuantity(quantity);
        item.setCheckInDate(checkInDate);
        item.setCheckOutDate(checkOutDate);
        item.setUseDate(useDate);
        
        // 根据类型获取商品信息和价格
        BigDecimal price = BigDecimal.ZERO;
        String itemName = "";
        
        switch (itemType) {
            case "ATTRACTION":
                Attraction attraction = attractionMapper.selectById(itemId);
                if (attraction == null || attraction.getStatus() == null || attraction.getStatus() != 1) {
                    throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "景点不存在或已下架");
                }
                price = attraction.getTicketPrice() != null ? attraction.getTicketPrice() : BigDecimal.ZERO;
                itemName = attraction.getName();
                break;
            case "HOTEL_ROOM":
                HotelRoom room = hotelRoomMapper.selectById(itemId);
                if (room == null || room.getStatus() == null || room.getStatus() != 1) {
                    throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "房型不存在或已下架");
                }
                price = room.getPrice() != null ? room.getPrice() : BigDecimal.ZERO;
                itemName = room.getRoomType();
                break;
            case "PRODUCT":
                Product product = productMapper.selectById(itemId);
                if (product == null || product.getStatus() == null || product.getStatus() != 1) {
                    throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "商品不存在或已下架");
                }
                price = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
                itemName = product.getName();
                // 保存商品图片快照
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    item.setItemImage(product.getImages().get(0));
                }
                // 保存商品编码快照
                item.setItemCode(product.getCode());
                break;
            default:
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "不支持的订单类型: " + itemType);
        }
        
        item.setPrice(price);
        item.setItemName(itemName);
        item.setTotalPrice(price.multiply(BigDecimal.valueOf(quantity)));
        
        return item;
    }
    
    /**
     * 验证库存
     */
    private void validateStock(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            switch (item.getItemType()) {
                case "ATTRACTION":
                    Attraction attraction = attractionMapper.selectById(item.getItemId());
                    if (attraction == null) {
                        throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "景点不存在");
                    }
                    if (attraction.getTicketStock() == null || attraction.getTicketStock() < item.getQuantity()) {
                        throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), 
                            "景点库存不足: " + item.getItemName());
                    }
                    break;
                case "HOTEL_ROOM":
                    HotelRoom room = hotelRoomMapper.selectById(item.getItemId());
                    if (room == null) {
                        throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "房型不存在");
                    }
                    if (room.getStock() == null || room.getStock() < item.getQuantity()) {
                        throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), 
                            "房型库存不足: " + item.getItemName());
                    }
                    break;
                case "PRODUCT":
                    Product product = productMapper.selectById(item.getItemId());
                    if (product == null) {
                        throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "商品不存在");
                    }
                    if (product.getStock() == null || product.getStock() < item.getQuantity()) {
                        throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), 
                            "商品库存不足: " + item.getItemName());
                    }
                    break;
            }
        }
    }
    
    /**
     * 计算订单总金额
     */
    private BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }
    
    /**
     * 验证和使用优惠券
     */
    private UserCoupon validateAndUseCoupon(Long userId, Long couponId, BigDecimal totalAmount, List<OrderItem> orderItems) {
        // 查询用户优惠券
        UserCoupon userCoupon = userCouponMapper.selectById(couponId);
        if (userCoupon == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "优惠券不存在");
        }
        
        if (!userCoupon.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权使用该优惠券");
        }
        
        if (userCoupon.getStatus() != null && userCoupon.getStatus() != 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "优惠券已使用或已过期");
        }
        
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND.getCode(), "优惠券信息不存在");
        }
        
        // 验证优惠券状态
        if (coupon.getStatus() == null || coupon.getStatus() != 1) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "优惠券已禁用");
        }
        
        // 验证有效期
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidStartTime() != null && coupon.getValidStartTime().isAfter(now)) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "优惠券尚未生效");
        }
        if (coupon.getValidEndTime() != null && coupon.getValidEndTime().isBefore(now)) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "优惠券已过期");
        }
        
        // 验证最低使用金额
        if (coupon.getMinAmount() != null && totalAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), 
                "订单金额未达到优惠券使用条件，最低金额: " + coupon.getMinAmount());
        }
        
        // 验证适用范围
        if (!validateCouponScope(coupon, orderItems)) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "优惠券不适用于当前订单");
        }
        
        // 验证首次满减券
        if ("FIRST_ORDER_REDUCTION".equals(coupon.getType())) {
            User user = userMapper.selectById(userId);
            if (user == null || user.getIsFirstOrder() == null || user.getIsFirstOrder() != 1) {
                throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), "首次满减券仅限首次下单用户使用");
            }
        }
        
        return userCoupon;
    }
    
    /**
     * 验证优惠券适用范围
     */
    private boolean validateCouponScope(Coupon coupon, List<OrderItem> orderItems) {
        if (coupon.getScope() == null || "ALL".equals(coupon.getScope())) {
            return true;
        }
        
        if (coupon.getScopeIds() == null || coupon.getScopeIds().isEmpty()) {
            return false;
        }
        
        try {
            List<Long> scopeIds = objectMapper.readValue(coupon.getScopeIds(), new TypeReference<List<Long>>() {});
            
            for (OrderItem item : orderItems) {
                if ("PRODUCT".equals(coupon.getScope())) {
                    // 指定商品：检查商品ID是否在范围内
                    if (scopeIds.contains(item.getItemId())) {
                        return true;
                    }
                } else if ("CATEGORY".equals(coupon.getScope())) {
                    // 指定分类：检查商品的分类ID是否在范围内
                    if ("PRODUCT".equals(item.getItemType())) {
                        Product product = productMapper.selectById(item.getItemId());
                        if (product != null && product.getCategoryId() != null) {
                            if (scopeIds.contains(product.getCategoryId())) {
                                return true;
                            }
                        }
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            log.error("解析优惠券适用范围失败", e);
            return false;
        }
    }
    
    /**
     * 计算优惠券折扣金额
     */
    private BigDecimal calculateCouponDiscount(Coupon coupon, BigDecimal totalAmount) {
        if (coupon.getAmount() == null) {
            return BigDecimal.ZERO;
        }
        
        switch (coupon.getType()) {
            case "FULL_REDUCTION":
            case "FIRST_ORDER_REDUCTION":
                // 满减券，直接减去面额
                return coupon.getAmount();
            case "DISCOUNT":
                // 折扣券，计算折扣金额
                BigDecimal discount = totalAmount.multiply(coupon.getAmount()).divide(BigDecimal.valueOf(100));
                return discount;
            case "FREE_SHIPPING":
                // 免运费券，这里不处理运费，返回0
                return BigDecimal.ZERO;
            default:
                return BigDecimal.ZERO;
        }
    }
    
    /**
     * 更新库存
     */
    private void updateStock(List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            switch (item.getItemType()) {
                case "ATTRACTION":
                    Attraction attraction = attractionMapper.selectById(item.getItemId());
                    if (attraction != null) {
                        int newStock = (attraction.getTicketStock() != null ? attraction.getTicketStock() : 0) - item.getQuantity();
                        attraction.setTicketStock(newStock);
                        attractionMapper.updateById(attraction);
                    }
                    break;
                case "HOTEL_ROOM":
                    HotelRoom room = hotelRoomMapper.selectById(item.getItemId());
                    if (room != null) {
                        int newStock = (room.getStock() != null ? room.getStock() : 0) - item.getQuantity();
                        room.setStock(newStock);
                        hotelRoomMapper.updateById(room);
                    }
                    break;
                case "PRODUCT":
                    Product product = productMapper.selectById(item.getItemId());
                    if (product != null) {
                        int newStock = (product.getStock() != null ? product.getStock() : 0) - item.getQuantity();
                        product.setStock(newStock);
                        productMapper.updateById(product);
                        // 增加销量
                        productMapper.increaseSales(item.getItemId(), item.getQuantity());
                    }
                    break;
            }
        }
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        // 格式：ORDER + 时间戳 + 随机字符串
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORDER" + timestamp + random;
    }
}
