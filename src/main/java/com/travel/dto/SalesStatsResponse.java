package com.travel.dto;

import lombok.Data;
import java.util.List;

/**
 * 销售统计数据响应DTO
 * 
 * @author travel-platform
 */
@Data
public class SalesStatsResponse {
    
    /**
     * 日期数组（格式：MM/dd）
     */
    private List<String> dates;
    
    /**
     * 销售额数组
     */
    private List<java.math.BigDecimal> values;
}
