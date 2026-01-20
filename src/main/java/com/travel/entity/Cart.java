package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 购物车实体类
 * 
 * @author travel-platform
 */
@Data
public class Cart implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 商品类型：ATTRACTION-景点，HOTEL_ROOM-酒店房型，PRODUCT-商品
     */
    private String itemType;
    
    /**
     * 商品ID
     */
    private Long itemId;
    
    /**
     * 数量
     */
    private Integer quantity;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
