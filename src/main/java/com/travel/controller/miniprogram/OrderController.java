package com.travel.controller.miniprogram;

import com.travel.common.Result;
import com.travel.common.ResultCode;
import com.travel.dto.OrderCreateRequest;
import com.travel.dto.OrderStatisticsResponse;
import com.travel.dto.PageResult;
import com.travel.dto.PaymentRequest;
import com.travel.entity.Order;
import com.travel.entity.User;
import com.travel.mapper.UserMapper;
import com.travel.service.OrderService;
import com.travel.service.PaymentService;
import com.travel.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 小程序订单控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/miniprogram/orders")
@Tag(name = "小程序订单")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 获取订单统计数量
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取订单统计数量")
    public Result<OrderStatisticsResponse> getOrderStatistics() {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            log.warn("获取订单统计失败：用户未登录或Session无效");
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        try {
            OrderStatisticsResponse result = orderService.getOrderStatistics(userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取订单统计失败：userId={}", userId, e);
            throw e;
        }
    }
    
    /**
     * 获取订单列表（按状态分类）
     */
    @GetMapping
    @Operation(summary = "获取订单列表")
    public Result<PageResult<Order>> getOrderList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        PageResult<Order> result = orderService.getUserOrders(userId, status, page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情")
    public Result<Order> getOrderDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        Order order = orderService.getOrderDetail(id, userId);
        return Result.success(order);
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消订单")
    public Result<?> cancelOrder(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        orderService.cancelOrder(id, userId);
        return Result.success("订单已取消");
    }
    
    /**
     * 申请退款
     */
    @PostMapping("/{id}/refund")
    @Operation(summary = "申请退款")
    public Result<?> applyRefund(@PathVariable Long id,
                                 @RequestParam(required = false) String reason) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        orderService.applyRefund(id, userId, reason);
        return Result.success("退款申请已提交");
    }
    
    /**
     * 创建订单
     */
    @PostMapping
    @Operation(summary = "创建订单")
    public Result<Order> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        Order order = orderService.createOrder(userId, request);
        return Result.success(order);
    }
    
    /**
     * 支付订单
     */
    @PostMapping("/{id}/pay")
    @Operation(summary = "支付订单")
    public Result<Map<String, String>> payOrder(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        // 查询订单
        Order order = orderService.getOrderDetail(id, userId);
        if (order == null) {
            return Result.error(ResultCode.DATA_NOT_FOUND);
        }
        
        // 验证订单状态
        if (!"PENDING_PAY".equals(order.getStatus())) {
            return Result.error(ResultCode.OPERATION_FAILED.getCode(), "订单状态不正确，无法支付");
        }
        
        // 获取用户openid
        User user = userMapper.selectById(userId);
        if (user == null || user.getOpenid() == null) {
            return Result.error(ResultCode.OPERATION_FAILED.getCode(), "用户信息不完整");
        }
        
        // 创建支付订单
        Map<String, String> paymentParams;
        if ("WECHAT".equals(request.getPayType())) {
            paymentParams = paymentService.createWechatPayment(order, user.getOpenid());
        } else {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "不支持的支付方式");
        }
        
        return Result.success(paymentParams);
    }
    
    /**
     * 微信支付回调
     */
    @PostMapping("/payment/notify")
    @Operation(summary = "微信支付回调")
    public String paymentNotify(@RequestBody String xmlData) {
        try {
            paymentService.handlePaymentNotify(xmlData);
            return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
        } catch (Exception e) {
            log.error("处理支付回调失败", e);
            return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[处理失败]]></return_msg></xml>";
        }
    }
}
