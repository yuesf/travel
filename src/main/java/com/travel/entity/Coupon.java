package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体类
 * 
 * @author travel-platform
 */
@Data
public class Coupon implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 优惠券名称
     */
    private String name;
    
    /**
     * 优惠券类型：FULL_REDUCE-满减，DISCOUNT-折扣，FREE_SHIPPING-免运费，FIRST_ORDER-首次满减
     */
    private String type;
    
    /**
     * 面额/折扣值
     */
    private BigDecimal amount;
    
    /**
     * 最低使用金额
     */
    private BigDecimal minAmount;
    
    /**
     * 适用范围：ALL-全场，PRODUCT-指定商品，CATEGORY-指定分类
     */
    private String scope;
    
    /**
     * 适用范围ID列表（JSON数组）
     */
    private String scopeIds;
    
    /**
     * 发放总数
     */
    private Integer totalCount;
    
    /**
     * 已使用数量
     */
    private Integer usedCount;
    
    /**
     * 每人限领数量
     */
    private Integer limitPerUser;
    
    /**
     * 有效期开始时间
     */
    private LocalDateTime validStartTime;
    
    /**
     * 有效期结束时间
     */
    private LocalDateTime validEndTime;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
