package com.travel.controller.admin;

import com.travel.common.CacheConstants;
import com.travel.common.Result;
import com.travel.dto.CacheRefreshRequest;
import com.travel.dto.CacheRefreshResponse;
import com.travel.service.CacheRefreshResult;
import com.travel.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 缓存管理控制器
 * 提供缓存管理的 RESTful API 接口
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/cache")
@Tag(name = "缓存管理", description = "缓存管理相关接口（管理员）")
public class CacheManagementController {

    @Autowired
    private CacheService cacheService;

    /**
     * 清除所有缓存
     * 
     * @return 清除结果
     */
    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "清除所有缓存", description = "清除系统中所有业务缓存")
    public Result<String> clearAll() {
        try {
            log.info("管理员请求清除所有缓存");
            cacheService.clearAll();
            return Result.success("所有缓存已清除");
        } catch (Exception e) {
            log.error("清除所有缓存失败", e);
            return Result.error("清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除指定类型的缓存
     * 
     * @param cacheType 缓存类型
     * @return 清除结果
     */
    @DeleteMapping("/{cacheType}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "清除指定类型缓存", description = "清除指定类型的缓存数据")
    @Parameter(name = "cacheType", description = "缓存类型", required = true, 
               example = "product")
    public Result<String> clearByType(@PathVariable String cacheType) {
        try {
            log.info("管理员请求清除缓存，类型: {}", cacheType);
            cacheService.clearByType(cacheType);
            return Result.success("缓存已清除: " + cacheType);
        } catch (IllegalArgumentException e) {
            log.warn("无效的缓存类型: {}", cacheType);
            return Result.error("无效的缓存类型: " + cacheType);
        } catch (Exception e) {
            log.error("清除缓存失败，类型: {}", cacheType, e);
            return Result.error("清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计数据
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取缓存统计", description = "获取所有缓存的统计信息（命中率、大小等）")
    public Result<Map<String, Object>> getStats() {
        try {
            log.info("管理员请求查询缓存统计信息");
            Map<String, Object> stats = cacheService.getCacheStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取缓存统计失败", e);
            return Result.error("获取缓存统计失败: " + e.getMessage());
        }
    }

    /**
     * 清除指定ID的商品缓存
     * 
     * @param id 商品ID
     * @return 清除结果
     */
    @DeleteMapping("/product/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "清除指定商品缓存", description = "清除指定ID的商品详情缓存")
    @Parameter(name = "id", description = "商品ID", required = true)
    public Result<String> clearProductDetail(@PathVariable Long id) {
        try {
            log.info("管理员请求清除商品缓存，ID: {}", id);
            cacheService.evictProductDetail(id);
            return Result.success("商品缓存已清除，ID: " + id);
        } catch (Exception e) {
            log.error("清除商品缓存失败，ID: {}", id, e);
            return Result.error("清除商品缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除指定ID的文章缓存
     * 
     * @param id 文章ID
     * @return 清除结果
     */
    @DeleteMapping("/article/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "清除指定文章缓存", description = "清除指定ID的文章详情缓存")
    @Parameter(name = "id", description = "文章ID", required = true)
    public Result<String> clearArticleDetail(@PathVariable Long id) {
        try {
            log.info("管理员请求清除文章缓存，ID: {}", id);
            cacheService.evictArticleDetail(id);
            return Result.success("文章缓存已清除，ID: " + id);
        } catch (Exception e) {
            log.error("清除文章缓存失败，ID: {}", id, e);
            return Result.error("清除文章缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除指定ID的景点缓存
     * 
     * @param id 景点ID
     * @return 清除结果
     */
    @DeleteMapping("/attraction/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "清除指定景点缓存", description = "清除指定ID的景点详情缓存")
    @Parameter(name = "id", description = "景点ID", required = true)
    public Result<String> clearAttractionDetail(@PathVariable Long id) {
        try {
            log.info("管理员请求清除景点缓存，ID: {}", id);
            cacheService.evictAttractionDetail(id);
            return Result.success("景点缓存已清除，ID: " + id);
        } catch (Exception e) {
            log.error("清除景点缓存失败，ID: {}", id, e);
            return Result.error("清除景点缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除首页缓存
     * 
     * @return 清除结果
     */
    @DeleteMapping("/home")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "清除首页缓存", description = "清除首页数据缓存")
    public Result<String> clearHome() {
        try {
            log.info("管理员请求清除首页缓存");
            cacheService.evictHome();
            return Result.success("首页缓存已清除");
        } catch (Exception e) {
            log.error("清除首页缓存失败", e);
            return Result.error("清除首页缓存失败: " + e.getMessage());
        }
    }

    /**
     * 手动刷新缓存（同步模式）
     * 
     * @param request 刷新请求
     * @return 刷新结果
     */
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "手动刷新缓存", description = "手动刷新指定类型的缓存数据（同步模式）")
    public Result<CacheRefreshResponse> refresh(@Valid @RequestBody CacheRefreshRequest request) {
        try {
            // 获取当前用户
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("管理员请求刷新缓存: user={}, cacheType={}, ids={}, async={}", 
                    username, request.getCacheType(), request.getIds(), request.getAsync());
            
            // 如果是异步模式，返回提示（异步功能在任务6.3实现）
            if (request.getAsync() != null && request.getAsync()) {
                return Result.error("异步刷新功能尚未实现，请使用同步模式");
            }
            
            // 检查数据量，如果超过阈值建议使用异步
            if (request.getIds() != null && request.getIds().size() > CacheConstants.ASYNC_THRESHOLD) {
                log.warn("数据量较大（{}），建议使用异步模式", request.getIds().size());
            }
            
            // 根据缓存类型调用对应的刷新方法
            CacheRefreshResult result;
            
            switch (request.getCacheType()) {
                case "home":
                    result = cacheService.refreshHome();
                    break;
                    
                case "miniprogram":
                    result = cacheService.refreshMiniprogramConfig();
                    break;
                    
                case "payment":
                    result = cacheService.refreshPaymentConfig();
                    break;
                    
                case "product":
                    if (request.getIds() != null && !request.getIds().isEmpty()) {
                        result = cacheService.refreshProducts(request.getIds());
                    } else {
                        return Result.error("商品刷新需要提供ID列表或使用全量刷新接口");
                    }
                    break;
                    
                case "article":
                    if (request.getIds() != null && !request.getIds().isEmpty()) {
                        result = cacheService.refreshArticles(request.getIds());
                    } else {
                        return Result.error("文章刷新需要提供ID列表或使用全量刷新接口");
                    }
                    break;
                    
                case "attraction":
                    if (request.getIds() != null && !request.getIds().isEmpty()) {
                        result = cacheService.refreshAttractions(request.getIds());
                    } else {
                        return Result.error("景点刷新需要提供ID列表或使用全量刷新接口");
                    }
                    break;
                    
                default:
                    return Result.error("不支持的缓存类型: " + request.getCacheType());
            }
            
            // 转换为响应DTO
            CacheRefreshResponse response = CacheRefreshResponse.createSyncResponse(
                    request.getCacheType(),
                    result.getTotalCount(),
                    result.getSuccessCount(),
                    result.getFailedCount(),
                    result.getDuration()
            );
            
            log.info("缓存刷新成功: user={}, cacheType={}, total={}, success={}, failed={}, duration={}ms", 
                    username, request.getCacheType(), result.getTotalCount(), 
                    result.getSuccessCount(), result.getFailedCount(), result.getDuration());
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("缓存刷新失败: cacheType={}", request.getCacheType(), e);
            return Result.error("缓存刷新失败: " + e.getMessage());
        }
    }
}
