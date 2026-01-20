package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.*;
import com.travel.entity.SyncConfig;
import com.travel.entity.SyncLog;
import com.travel.service.AttractionSyncService;
import com.travel.service.SyncConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 景点同步管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/attractions/sync")
@Tag(name = "景点同步管理")
public class AttractionSyncController {
    
    @Autowired
    private SyncConfigService syncConfigService;
    
    @Autowired
    private AttractionSyncService attractionSyncService;
    
    /**
     * 查询所有同步配置
     */
    @GetMapping("/configs")
    @Operation(summary = "查询所有同步配置")
    public Result<List<SyncConfig>> listConfigs() {
        List<SyncConfig> configs = syncConfigService.listAll();
        return Result.success(configs);
    }
    
    /**
     * 根据ID查询同步配置
     */
    @GetMapping("/configs/{id}")
    @Operation(summary = "查询同步配置详情")
    public Result<SyncConfig> getConfig(@PathVariable Long id) {
        SyncConfig config = syncConfigService.getById(id);
        return Result.success(config);
    }
    
    /**
     * 创建同步配置
     */
    @PostMapping("/configs")
    @Operation(summary = "创建同步配置")
    public Result<SyncConfig> createConfig(@Valid @RequestBody SyncConfigCreateRequest request) {
        SyncConfig config = syncConfigService.create(request);
        return Result.success(config);
    }
    
    /**
     * 更新同步配置
     */
    @PutMapping("/configs/{id}")
    @Operation(summary = "更新同步配置")
    public Result<SyncConfig> updateConfig(@PathVariable Long id, 
                                          @Valid @RequestBody SyncConfigUpdateRequest request) {
        SyncConfig config = syncConfigService.update(id, request);
        return Result.success(config);
    }
    
    /**
     * 删除同步配置
     */
    @DeleteMapping("/configs/{id}")
    @Operation(summary = "删除同步配置")
    public Result<?> deleteConfig(@PathVariable Long id) {
        syncConfigService.delete(id);
        return Result.success();
    }
    
    /**
     * 手动同步景点数据
     */
    @PostMapping("/manual/{configId}")
    @Operation(summary = "手动同步景点数据")
    public Result<SyncLog> manualSync(@PathVariable Long configId) {
        SyncLog syncLog = attractionSyncService.manualSync(configId);
        return Result.success(syncLog);
    }
    
    /**
     * 分页查询同步日志
     */
    @GetMapping("/logs")
    @Operation(summary = "分页查询同步日志")
    public Result<PageResult<SyncLog>> listLogs(SyncLogListRequest request) {
        PageResult<SyncLog> result = syncConfigService.listLogs(request);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询同步日志详情
     */
    @GetMapping("/logs/{id}")
    @Operation(summary = "查询同步日志详情")
    public Result<SyncLog> getLog(@PathVariable Long id) {
        // 这里需要添加查询单个日志的方法到SyncConfigService
        // 暂时使用getById方法
        return Result.success();
    }
}
