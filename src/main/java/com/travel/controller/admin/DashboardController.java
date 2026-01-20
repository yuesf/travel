package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.DashboardStatsResponse;
import com.travel.dto.OrderTrendResponse;
import com.travel.dto.SalesStatsResponse;
import com.travel.service.CacheService;
import com.travel.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 仪表盘控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController("adminDashboardController")
@RequestMapping("/api/v1/admin/dashboard")
@Tag(name = "仪表盘管理")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private CacheService cacheService;
    
    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取仪表盘统计数据")
    public Result<DashboardStatsResponse> getStats() {
        DashboardStatsResponse response = dashboardService.getStats();
        return Result.success(response);
    }
    
    /**
     * 获取订单趋势数据
     */
    @GetMapping("/order-trend")
    @Operation(summary = "获取订单趋势数据")
    public Result<OrderTrendResponse> getOrderTrend(
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        OrderTrendResponse response = dashboardService.getOrderTrend(days);
        return Result.success(response);
    }
    
    /**
     * 获取销售统计数据
     */
    @GetMapping("/sales-stats")
    @Operation(summary = "获取销售统计数据")
    public Result<SalesStatsResponse> getSalesStats(
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        SalesStatsResponse response = dashboardService.getSalesStats(days);
        return Result.success(response);
    }
    
    /**
     * 清除所有缓存
     */
    @PostMapping("/cache/clear-all")
    @Operation(summary = "清除所有缓存")
    public Result<?> clearAllCache() {
        cacheService.clearAll();
        return Result.success("缓存清除成功");
    }
    
    /**
     * 清除指定类型的缓存
     */
    @PostMapping("/cache/clear")
    @Operation(summary = "清除指定类型的缓存")
    public Result<?> clearCache(@RequestParam String cacheType) {
        cacheService.clearByType(cacheType);
        return Result.success("缓存清除成功");
    }
    
    /**
     * 获取缓存统计信息
     */
    @GetMapping("/cache/stats")
    @Operation(summary = "获取缓存统计信息")
    public Result<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = cacheService.getCacheStats();
        return Result.success(stats);
    }
}
