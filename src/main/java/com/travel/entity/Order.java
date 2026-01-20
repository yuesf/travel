package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类
 * 
 * @author travel-platform
 */
@Data
public class Order implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 订单类型：ATTRACTION-景点，HOTEL-酒店，PRODUCT-商品
     */
    private String orderType;
    
    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;
    
    /**
     * 实付金额
     */
    private BigDecimal payAmount;
    
    /**
     * 使用的优惠券ID
     */
    private Long couponId;
    
    /**
     * 订单状态：PENDING_PAY-待支付，PAID-已支付，USED-已使用，COMPLETED-已完成，CANCELLED-已取消，REFUNDED-已退款
     */
    private String status;
    
    /**
     * 支付时间
     */
    private LocalDateTime payTime;
    
    /**
     * 支付方式：WECHAT-微信支付
     */
    private String payType;
    
    /**
     * 支付流水号
     */
    private String payNo;
    
    /**
     * 联系人姓名
     */
    private String contactName;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 订单项列表（不持久化到数据库，仅用于返回给前端）
     */
    private transient List<OrderItem> items;
}
