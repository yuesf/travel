package com.travel.service;

import com.travel.dto.DashboardStatsResponse;
import com.travel.dto.OrderTrendResponse;
import com.travel.dto.SalesStatsResponse;
import com.travel.mapper.StatisticsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class DashboardService {
    
    @Autowired
    private StatisticsMapper statisticsMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("M/d");
    
    /**
     * 获取仪表盘统计数据
     */
    public DashboardStatsResponse getStats() {
        DashboardStatsResponse response = new DashboardStatsResponse();
        
        // 获取今天的开始和结束时间
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        String todayStart = today.format(DATE_FORMATTER) + " 00:00:00";
        String todayEnd = today.format(DATE_FORMATTER) + " 23:59:59";
        String yesterdayStart = yesterday.format(DATE_FORMATTER) + " 00:00:00";
        String yesterdayEnd = yesterday.format(DATE_FORMATTER) + " 23:59:59";
        
        // 今日订单数
        Long todayOrders = statisticsMapper.countOrders(todayStart, todayEnd);
        response.setTodayOrders(todayOrders != null ? todayOrders : 0L);
        
        // 昨日订单数（用于计算变化率）
        Long yesterdayOrders = statisticsMapper.countOrders(yesterdayStart, yesterdayEnd);
        yesterdayOrders = yesterdayOrders != null ? yesterdayOrders : 0L;
        
        // 计算今日订单变化率
        if (yesterdayOrders > 0) {
            double change = ((double)(todayOrders - yesterdayOrders) / yesterdayOrders) * 100;
            response.setTodayOrdersChange(Math.round(change * 10.0) / 10.0);
        } else if (todayOrders > 0) {
            response.setTodayOrdersChange(100.0);
        } else {
            response.setTodayOrdersChange(0.0);
        }
        
        // 今日销售额（已支付订单金额）
        BigDecimal todaySales = statisticsMapper.sumPaidOrderAmount(todayStart, todayEnd);
        response.setTodaySales(todaySales != null ? todaySales : BigDecimal.ZERO);
        
        // 昨日销售额（用于计算变化率）
        BigDecimal yesterdaySales = statisticsMapper.sumPaidOrderAmount(yesterdayStart, yesterdayEnd);
        yesterdaySales = yesterdaySales != null ? yesterdaySales : BigDecimal.ZERO;
        
        // 计算今日销售额变化率
        if (yesterdaySales.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal change = todaySales.subtract(yesterdaySales)
                    .divide(yesterdaySales, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            response.setTodaySalesChange(change.setScale(1, RoundingMode.HALF_UP).doubleValue());
        } else if (todaySales.compareTo(BigDecimal.ZERO) > 0) {
            response.setTodaySalesChange(100.0);
        } else {
            response.setTodaySalesChange(0.0);
        }
        
        // 待处理订单数
        Long pendingOrders = statisticsMapper.countPendingOrders();
        response.setPendingOrders(pendingOrders != null ? pendingOrders : 0L);
        
        // 总用户数（不限制时间）
        Long totalUsers = statisticsMapper.countUsers(null, null);
        response.setTotalUsers(totalUsers != null ? totalUsers : 0L);
        
        return response;
    }
    
    /**
     * 获取订单趋势数据
     * 
     * @param days 天数（7、30、90）
     */
    public OrderTrendResponse getOrderTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }
        
        // 计算时间范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        String startTime = startDate.format(DATE_FORMATTER) + " 00:00:00";
        String endTime = endDate.format(DATE_FORMATTER) + " 23:59:59";
        
        // 查询订单趋势数据
        List<Map<String, Object>> orderTrend = statisticsMapper.getOrderTrend(startTime, endTime);
        
        // 构建日期和值的映射
        Map<String, Long> trendMap = new java.util.HashMap<>();
        for (Map<String, Object> item : orderTrend) {
            // 处理 date 字段，可能是 java.sql.Date 或 String
            Object dateObj = item.get("date");
            String date;
            if (dateObj instanceof java.sql.Date) {
                date = ((java.sql.Date) dateObj).toLocalDate().format(DATE_FORMATTER);
            } else if (dateObj instanceof java.util.Date) {
                date = new java.text.SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) dateObj);
            } else {
                date = dateObj.toString();
            }
            Long count = ((Number) item.get("count")).longValue();
            trendMap.put(date, count);
        }
        
        // 填充所有日期（包括没有订单的日期）
        List<String> dates = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(DATE_FORMATTER);
            String displayDate = currentDate.format(DISPLAY_FORMATTER);
            dates.add(displayDate);
            values.add(trendMap.getOrDefault(dateStr, 0L));
            currentDate = currentDate.plusDays(1);
        }
        
        OrderTrendResponse response = new OrderTrendResponse();
        response.setDates(dates);
        response.setValues(values);
        
        return response;
    }
    
    /**
     * 获取销售统计数据
     * 
     * @param days 天数（7、30、90）
     */
    public SalesStatsResponse getSalesStats(Integer days) {
        if (days == null || days <= 0) {
            days = 7; // 默认7天
        }
        
        // 计算时间范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        String startTime = startDate.format(DATE_FORMATTER) + " 00:00:00";
        String endTime = endDate.format(DATE_FORMATTER) + " 23:59:59";
        
        // 查询销售趋势数据（使用已支付订单金额）
        List<Map<String, Object>> amountTrend = statisticsMapper.getPaidAmountTrend(startTime, endTime);
        
        // 构建日期和金额的映射
        Map<String, BigDecimal> trendMap = new java.util.HashMap<>();
        for (Map<String, Object> item : amountTrend) {
            // 处理 date 字段，可能是 java.sql.Date 或 String
            Object dateObj = item.get("date");
            String date;
            if (dateObj instanceof java.sql.Date) {
                date = ((java.sql.Date) dateObj).toLocalDate().format(DATE_FORMATTER);
            } else if (dateObj instanceof java.util.Date) {
                date = new java.text.SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) dateObj);
            } else {
                date = dateObj.toString();
            }
            BigDecimal amount = (BigDecimal) item.get("amount");
            if (amount == null) {
                amount = BigDecimal.ZERO;
            }
            trendMap.put(date, amount);
        }
        
        // 填充所有日期（包括没有订单的日期）
        List<String> dates = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(DATE_FORMATTER);
            String displayDate = currentDate.format(DISPLAY_FORMATTER);
            dates.add(displayDate);
            values.add(trendMap.getOrDefault(dateStr, BigDecimal.ZERO));
            currentDate = currentDate.plusDays(1);
        }
        
        SalesStatsResponse response = new SalesStatsResponse();
        response.setDates(dates);
        response.setValues(values);
        
        return response;
    }
}
