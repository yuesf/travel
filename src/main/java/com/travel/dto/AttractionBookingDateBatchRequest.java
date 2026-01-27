package com.travel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 批量设置可订日期请求DTO
 * 
 * @author travel-platform
 */
@Data
public class AttractionBookingDateBatchRequest {
    
    /**
     * 景点ID（从路径参数获取，不需要验证）
     */
    private Long attractionId;
    
    /**
     * 可订日期列表
     */
    @NotNull(message = "可订日期列表不能为空")
    private List<BookingDateItem> dates;
    
    @Data
    public static class BookingDateItem {
        /**
         * 可订日期
         */
        @NotNull(message = "可订日期不能为空")
        private LocalDate bookingDate;
        
        /**
         * 当日价格
         */
        @NotNull(message = "价格不能为空")
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
}
