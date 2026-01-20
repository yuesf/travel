package com.travel.service;

import com.travel.dto.MiniProgramStatisticsResponse;
import com.travel.mapper.StatisticsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小程序统计服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class MiniProgramStatisticsService {
    
    @Autowired
    private StatisticsMapper statisticsMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 获取小程序统计数据
     */
    public MiniProgramStatisticsResponse getStatistics(String startDate, String endDate) {
        // 解析时间范围
        String startTime = null;
        String endTime = null;
        if (startDate != null && !startDate.trim().isEmpty()) {
            startTime = startDate + " 00:00:00";
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            endTime = endDate + " 23:59:59";
        }
        
        // 如果没有指定时间范围，默认查询最近30天
        if (startTime == null || endTime == null) {
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(30);
            startTime = start.format(DATE_FORMATTER) + " 00:00:00";
            endTime = end.format(DATE_FORMATTER) + " 23:59:59";
        }
        
        MiniProgramStatisticsResponse response = new MiniProgramStatisticsResponse();
        
        // 访问统计（暂时使用订单数据估算，后续需要访问日志表）
        response.setAccess(buildAccessStatistics(startTime, endTime));
        
        // 订单统计
        response.setOrder(buildOrderStatistics(startTime, endTime));
        
        // 商品浏览统计
        response.setProduct(buildProductStatistics(startTime, endTime));
        
        // 用户统计
        response.setUser(buildUserStatistics(startTime, endTime));
        
        return response;
    }
    
    /**
     * 构建访问统计
     */
    private MiniProgramStatisticsResponse.AccessStatistics buildAccessStatistics(String startTime, String endTime) {
        MiniProgramStatisticsResponse.AccessStatistics statistics = new MiniProgramStatisticsResponse.AccessStatistics();
        
        // 暂时使用订单数估算PV，UV使用活跃用户数
        // 后续需要创建访问日志表来准确统计PV/UV
        Long orderCount = statisticsMapper.countOrders(startTime, endTime);
        Long activeUsers = statisticsMapper.countActiveUsers(startTime, endTime);
        
        // 估算PV：订单数 * 10（假设每个订单平均浏览10个页面）
        statistics.setPv(orderCount * 10);
        statistics.setUv(activeUsers);
        
        // 构建趋势数据（使用订单趋势数据）
        List<Map<String, Object>> orderTrend = statisticsMapper.getOrderTrend(startTime, endTime);
        List<Map<String, Object>> pvTrend = new ArrayList<>();
        List<Map<String, Object>> uvTrend = new ArrayList<>();
        
        for (Map<String, Object> item : orderTrend) {
            // 处理 date 字段，可能是 java.sql.Date 或 String
            Object dateObj = item.get("date");
            String date;
            if (dateObj instanceof java.sql.Date) {
                date = ((java.sql.Date) dateObj).toLocalDate().format(DATE_FORMATTER);
            } else if (dateObj instanceof java.util.Date) {
                date = new java.text.SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) dateObj);
            } else {
                date = dateObj != null ? dateObj.toString() : "";
            }
            Long count = ((Number) item.get("count")).longValue();
            
            Map<String, Object> pvItem = new HashMap<>();
            pvItem.put("date", date);
            pvItem.put("value", count * 10); // 估算PV
            pvTrend.add(pvItem);
            
            Map<String, Object> uvItem = new HashMap<>();
            uvItem.put("date", date);
            uvItem.put("value", count); // 使用订单数估算UV
            uvTrend.add(uvItem);
        }
        
        statistics.setPvTrend(pvTrend);
        statistics.setUvTrend(uvTrend);
        
        return statistics;
    }
    
    /**
     * 构建订单统计
     */
    private MiniProgramStatisticsResponse.OrderStatistics buildOrderStatistics(String startTime, String endTime) {
        MiniProgramStatisticsResponse.OrderStatistics statistics = new MiniProgramStatisticsResponse.OrderStatistics();
        
        // 统计订单数据
        Long totalOrders = statisticsMapper.countOrders(startTime, endTime);
        Long paidOrders = statisticsMapper.countPaidOrders(startTime, endTime);
        BigDecimal totalAmount = statisticsMapper.sumOrderAmount(startTime, endTime);
        BigDecimal paidAmount = statisticsMapper.sumPaidOrderAmount(startTime, endTime);
        
        statistics.setTotalOrders(totalOrders);
        statistics.setPaidOrders(paidOrders);
        statistics.setTotalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO);
        statistics.setPaidAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO);
        
        // 计算转化率（已支付订单数 / 活跃用户数）
        Long activeUsers = statisticsMapper.countActiveUsers(startTime, endTime);
        if (activeUsers != null && activeUsers > 0) {
            BigDecimal conversionRate = BigDecimal.valueOf(paidOrders)
                    .divide(BigDecimal.valueOf(activeUsers), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            statistics.setConversionRate(conversionRate);
        } else {
            statistics.setConversionRate(BigDecimal.ZERO);
        }
        
        // 构建趋势数据
        List<Map<String, Object>> orderTrend = statisticsMapper.getOrderTrend(startTime, endTime);
        List<Map<String, Object>> amountTrend = statisticsMapper.getAmountTrend(startTime, endTime);
        
        // 格式化趋势数据
        List<Map<String, Object>> formattedOrderTrend = new ArrayList<>();
        for (Map<String, Object> item : orderTrend) {
            Map<String, Object> formatted = new HashMap<>();
            // 处理 date 字段，可能是 java.sql.Date 或 String
            Object dateObj = item.get("date");
            String date;
            if (dateObj instanceof java.sql.Date) {
                date = ((java.sql.Date) dateObj).toLocalDate().format(DATE_FORMATTER);
            } else if (dateObj instanceof java.util.Date) {
                date = new java.text.SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) dateObj);
            } else {
                date = dateObj != null ? dateObj.toString() : "";
            }
            formatted.put("date", date);
            formatted.put("value", item.get("count"));
            formattedOrderTrend.add(formatted);
        }
        
        List<Map<String, Object>> formattedAmountTrend = new ArrayList<>();
        for (Map<String, Object> item : amountTrend) {
            Map<String, Object> formatted = new HashMap<>();
            // 处理 date 字段，可能是 java.sql.Date 或 String
            Object dateObj = item.get("date");
            String date;
            if (dateObj instanceof java.sql.Date) {
                date = ((java.sql.Date) dateObj).toLocalDate().format(DATE_FORMATTER);
            } else if (dateObj instanceof java.util.Date) {
                date = new java.text.SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) dateObj);
            } else {
                date = dateObj != null ? dateObj.toString() : "";
            }
            formatted.put("date", date);
            formatted.put("value", item.get("amount"));
            formattedAmountTrend.add(formatted);
        }
        
        statistics.setOrderTrend(formattedOrderTrend);
        statistics.setAmountTrend(formattedAmountTrend);
        
        return statistics;
    }
    
    /**
     * 构建商品浏览统计
     */
    private MiniProgramStatisticsResponse.ProductStatistics buildProductStatistics(String startTime, String endTime) {
        MiniProgramStatisticsResponse.ProductStatistics statistics = new MiniProgramStatisticsResponse.ProductStatistics();
        
        // 获取热门商品（前10）
        List<Map<String, Object>> hotProducts = statisticsMapper.getHotProducts(startTime, endTime, 10);
        
        // 格式化热门商品数据
        List<Map<String, Object>> formattedHotProducts = new ArrayList<>();
        long totalViews = 0;
        for (Map<String, Object> item : hotProducts) {
            Map<String, Object> formatted = new HashMap<>();
            formatted.put("type", item.get("type"));
            formatted.put("id", item.get("id"));
            formatted.put("name", item.get("name"));
            formatted.put("sales", item.get("sales"));
            formatted.put("amount", item.get("amount"));
            formattedHotProducts.add(formatted);
            
            totalViews += ((Number) item.get("sales")).longValue();
        }
        
        statistics.setTotalViews(totalViews);
        statistics.setHotProducts(formattedHotProducts);
        
        // 获取商品类型分布
        List<Map<String, Object>> typeDistribution = statisticsMapper.getProductTypeDistribution(startTime, endTime);
        List<Map<String, Object>> formattedTypeDistribution = new ArrayList<>();
        for (Map<String, Object> item : typeDistribution) {
            Map<String, Object> formatted = new HashMap<>();
            formatted.put("type", item.get("type"));
            formatted.put("count", item.get("count"));
            formatted.put("sales", item.get("sales"));
            formatted.put("amount", item.get("amount"));
            formattedTypeDistribution.add(formatted);
        }
        statistics.setTypeDistribution(formattedTypeDistribution);
        
        return statistics;
    }
    
    /**
     * 构建用户统计
     */
    private MiniProgramStatisticsResponse.UserStatistics buildUserStatistics(String startTime, String endTime) {
        MiniProgramStatisticsResponse.UserStatistics statistics = new MiniProgramStatisticsResponse.UserStatistics();
        
        // 统计用户数据
        Long totalUsers = statisticsMapper.countUsers(null, null); // 总用户数不限制时间
        Long activeUsers = statisticsMapper.countActiveUsers(startTime, endTime);
        Long newUsers = statisticsMapper.countNewUsers(startTime, endTime);
        
        statistics.setTotalUsers(totalUsers);
        statistics.setActiveUsers(activeUsers);
        statistics.setNewUsers(newUsers);
        
        // 计算用户留存率（活跃用户数 / 总用户数）
        if (totalUsers != null && totalUsers > 0) {
            BigDecimal retentionRate = BigDecimal.valueOf(activeUsers)
                    .divide(BigDecimal.valueOf(totalUsers), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            statistics.setRetentionRate(retentionRate);
        } else {
            statistics.setRetentionRate(BigDecimal.ZERO);
        }
        
        // 构建新增用户趋势数据
        List<Map<String, Object>> newUserTrend = statisticsMapper.getNewUserTrend(startTime, endTime);
        List<Map<String, Object>> formattedNewUserTrend = new ArrayList<>();
        for (Map<String, Object> item : newUserTrend) {
            Map<String, Object> formatted = new HashMap<>();
            // 处理 date 字段，可能是 java.sql.Date 或 String
            Object dateObj = item.get("date");
            String date;
            if (dateObj instanceof java.sql.Date) {
                date = ((java.sql.Date) dateObj).toLocalDate().format(DATE_FORMATTER);
            } else if (dateObj instanceof java.util.Date) {
                date = new java.text.SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) dateObj);
            } else {
                date = dateObj != null ? dateObj.toString() : "";
            }
            formatted.put("date", date);
            formatted.put("value", item.get("count"));
            formattedNewUserTrend.add(formatted);
        }
        statistics.setNewUserTrend(formattedNewUserTrend);
        
        return statistics;
    }
}
