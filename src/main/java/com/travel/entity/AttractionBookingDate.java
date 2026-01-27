package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 景点可订日期实体类
 * 
 * @author travel-platform
 */
@Data
public class AttractionBookingDate implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 景点ID
     */
    private Long attractionId;
    
    /**
     * 可订日期
     */
    private LocalDate bookingDate;
    
    /**
     * 当日价格
     */
    private BigDecimal price;
    
    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 是否可订：0-不可订，1-可订
     */
    private Integer available;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
