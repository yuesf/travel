package com.travel.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 小程序统计数据响应DTO
 * 
 * @author travel-platform
 */
@Data
public class MiniProgramStatisticsResponse {
    
    /**
     * 访问统计
     */
    private AccessStatistics access;
    
    /**
     * 订单统计
     */
    private OrderStatistics order;
    
    /**
     * 商品浏览统计
     */
    private ProductStatistics product;
    
    /**
     * 用户统计
     */
    private UserStatistics user;
    
    /**
     * 访问统计
     */
    @Data
    public static class AccessStatistics {
        /**
         * PV（页面访问量）
         */
        private Long pv;
        
        /**
         * UV（独立访客数）
         */
        private Long uv;
        
        /**
         * 每日PV趋势数据
         */
        private List<Map<String, Object>> pvTrend;
        
        /**
         * 每日UV趋势数据
         */
        private List<Map<String, Object>> uvTrend;
    }
    
    /**
     * 订单统计
     */
    @Data
    public static class OrderStatistics {
        /**
         * 总订单数
         */
        private Long totalOrders;
        
        /**
         * 总订单金额
         */
        private BigDecimal totalAmount;
        
        /**
         * 已支付订单数
         */
        private Long paidOrders;
        
        /**
         * 已支付订单金额
         */
        private BigDecimal paidAmount;
        
        /**
         * 订单转化率（订单数/访问用户数）
         */
        private BigDecimal conversionRate;
        
        /**
         * 每日订单趋势数据
         */
        private List<Map<String, Object>> orderTrend;
        
        /**
         * 每日订单金额趋势数据
         */
        private List<Map<String, Object>> amountTrend;
    }
    
    /**
     * 商品浏览统计
     */
    @Data
    public static class ProductStatistics {
        /**
         * 商品浏览总数
         */
        private Long totalViews;
        
        /**
         * 热门商品列表（前10）
         */
        private List<Map<String, Object>> hotProducts;
        
        /**
         * 商品类型分布
         */
        private List<Map<String, Object>> typeDistribution;
    }
    
    /**
     * 用户统计
     */
    @Data
    public static class UserStatistics {
        /**
         * 总用户数
         */
        private Long totalUsers;
        
        /**
         * 活跃用户数（有订单的用户）
         */
        private Long activeUsers;
        
        /**
         * 新增用户数
         */
        private Long newUsers;
        
        /**
         * 用户留存率
         */
        private BigDecimal retentionRate;
        
        /**
         * 每日新增用户趋势数据
         */
        private List<Map<String, Object>> newUserTrend;
    }
}
