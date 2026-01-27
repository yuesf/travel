package com.travel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 创建金顶时间段请求DTO
 * 
 * @author travel-platform
 */
@Data
public class AttractionGoldenSummitTimeSlotCreateRequest {
    
    /**
     * 景点ID（从路径参数获取，不需要验证）
     */
    private Long attractionId;
    
    /**
     * 日期
     */
    @NotNull(message = "日期不能为空")
    private LocalDate bookingDate;
    
    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;
    
    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;
    
    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 是否可用：0-不可用，1-可用
     */
    private Integer available;
}
