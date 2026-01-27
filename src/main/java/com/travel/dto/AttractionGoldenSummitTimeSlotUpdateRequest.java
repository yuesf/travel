package com.travel.dto;

import lombok.Data;

import java.time.LocalTime;

/**
 * 更新金顶时间段请求DTO
 * 
 * @author travel-platform
 */
@Data
public class AttractionGoldenSummitTimeSlotUpdateRequest {
    
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
}
