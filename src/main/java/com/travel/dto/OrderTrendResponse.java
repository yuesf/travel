package com.travel.dto;

import lombok.Data;
import java.util.List;

/**
 * 订单趋势数据响应DTO
 * 
 * @author travel-platform
 */
@Data
public class OrderTrendResponse {
    
    /**
     * 日期数组（格式：MM/dd）
     */
    private List<String> dates;
    
    /**
     * 订单数数组
     */
    private List<Long> values;
}
