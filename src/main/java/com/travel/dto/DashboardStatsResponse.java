package com.travel.dto;

import lombok.Data;

/**
 * 仪表盘统计数据响应DTO
 * 
 * @author travel-platform
 */
@Data
public class DashboardStatsResponse {
    
    /**
     * 今日订单数
     */
    private Long todayOrders;
    
    /**
     * 今日订单变化率（百分比，正数表示增长，负数表示下降）
     */
    private Double todayOrdersChange;
    
    /**
     * 今日销售额
     */
    private java.math.BigDecimal todaySales;
    
    /**
     * 今日销售额变化率（百分比，正数表示增长，负数表示下降）
     */
    private Double todaySalesChange;
    
    /**
     * 待处理订单数
     */
    private Long pendingOrders;
    
    /**
     * 总用户数
     */
    private Long totalUsers;
}
