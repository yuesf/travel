package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 金顶时间段实体类
 * 
 * @author travel-platform
 */
@Data
public class AttractionGoldenSummitTimeSlot implements Serializable {
    
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
     * 日期
     */
    private LocalDate bookingDate;
    
    /**
     * 开始时间
     */
    private LocalTime startTime;
    
    /**
     * 结束时间
     */
    private LocalTime endTime;
    
    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 是否可用：0-不可用，1-可用
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
