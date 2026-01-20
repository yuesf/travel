package com.travel.mapper;

import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计数据Mapper接口
 * 
 * @author travel-platform
 */
public interface StatisticsMapper {
    
    /**
     * 统计总订单数
     */
    Long countOrders(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 统计已支付订单数
     */
    Long countPaidOrders(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 统计待处理订单数（PENDING_PAY状态）
     */
    Long countPendingOrders();
    
    /**
     * 统计订单总金额
     */
    BigDecimal sumOrderAmount(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 统计已支付订单总金额
     */
    BigDecimal sumPaidOrderAmount(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 统计每日订单数趋势
     */
    List<Map<String, Object>> getOrderTrend(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 统计每日订单金额趋势
     */
    List<Map<String, Object>> getAmountTrend(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 统计每日已支付订单金额趋势
     */
    List<Map<String, Object>> getPaidAmountTrend(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 统计总用户数
     */
    Long countUsers(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 统计活跃用户数（有订单的用户）
     */
    Long countActiveUsers(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 统计新增用户数
     */
    Long countNewUsers(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 统计每日新增用户趋势
     */
    List<Map<String, Object>> getNewUserTrend(@Param("startTime") String startTime, @Param("endTime") String endTime);
    
    /**
     * 统计热门商品（前10）
     */
    List<Map<String, Object>> getHotProducts(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("limit") Integer limit);
    
    /**
     * 统计商品类型分布
     */
    List<Map<String, Object>> getProductTypeDistribution(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
