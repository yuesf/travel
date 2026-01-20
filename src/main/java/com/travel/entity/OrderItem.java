package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单明细实体类
 * 
 * @author travel-platform
 */
@Data
public class OrderItem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 明细类型：ATTRACTION-景点，HOTEL_ROOM-酒店房型，PRODUCT-商品
     */
    private String itemType;
    
    /**
     * 明细ID（景点ID/房型ID/商品ID）
     */
    private Long itemId;
    
    /**
     * 明细名称
     */
    private String itemName;
    
    /**
     * 商品图片URL（订单创建时的快照）
     */
    private String itemImage;
    
    /**
     * 商品编码（订单创建时的快照）
     */
    private String itemCode;
    
    /**
     * 数量
     */
    private Integer quantity;
    
    /**
     * 单价
     */
    private BigDecimal price;
    
    /**
     * 小计
     */
    private BigDecimal totalPrice;
    
    /**
     * 入住日期（酒店）
     */
    private LocalDate checkInDate;
    
    /**
     * 退房日期（酒店）
     */
    private LocalDate checkOutDate;
    
    /**
     * 使用日期（景点）
     */
    private LocalDate useDate;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
