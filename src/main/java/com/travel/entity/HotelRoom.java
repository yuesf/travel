package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 酒店房型实体类
 * 
 * @author travel-platform
 */
@Data
public class HotelRoom implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 酒店ID
     */
    private Long hotelId;
    
    /**
     * 房型名称
     */
    private String roomType;
    
    /**
     * 价格
     */
    private BigDecimal price;
    
    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 床型
     */
    private String bedType;
    
    /**
     * 面积（平方米）
     */
    private BigDecimal area;
    
    /**
     * 设施列表（JSON数组）
     */
    private List<String> facilities;
    
    /**
     * 图片列表（JSON数组）
     */
    private List<String> images;
    
    /**
     * 状态：0-下架，1-上架
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
