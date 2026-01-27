package com.travel.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新可订日期请求DTO
 * 
 * @author travel-platform
 */
@Data
public class AttractionBookingDateUpdateRequest {
    
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
}
