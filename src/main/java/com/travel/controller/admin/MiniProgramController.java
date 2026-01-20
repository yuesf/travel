package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.MiniProgramConfigCreateRequest;
import com.travel.dto.MiniProgramConfigUpdateRequest;
import com.travel.entity.MiniProgramConfig;
import com.travel.service.MiniProgramConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 小程序管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/miniprogram")
@Tag(name = "小程序管理")
public class MiniProgramController {
    
    @Autowired
    private MiniProgramConfigService miniProgramConfigService;
    
    @Autowired
    private com.travel.service.MiniProgramStatisticsService miniProgramStatisticsService;
    
    /**
     * 获取小程序配置
     */
    @GetMapping("/config")
    @Operation(summary = "获取小程序配置")
    public Result<List<MiniProgramConfig>> getConfig(
            @RequestParam(required = false) String configType,
            @RequestParam(required = false) Integer status) {
        List<MiniProgramConfig> configs = miniProgramConfigService.getAll(configType, status);
        return Result.success(configs);
    }
    
    /**
     * 根据配置键获取配置
     */
    @GetMapping("/config/{configKey}")
    @Operation(summary = "根据配置键获取配置")
    public Result<MiniProgramConfig> getConfigByKey(@PathVariable String configKey) {
        MiniProgramConfig config = miniProgramConfigService.getByConfigKey(configKey);
        return Result.success(config);
    }
    
    /**
     * 根据配置类型获取配置列表
     */
    @GetMapping("/config/type/{configType}")
    @Operation(summary = "根据配置类型获取配置列表")
    public Result<List<MiniProgramConfig>> getConfigByType(
            @PathVariable String configType,
            @RequestParam(required = false) Integer status) {
        List<MiniProgramConfig> configs = miniProgramConfigService.getByConfigType(configType, status);
        return Result.success(configs);
    }
    
    /**
     * 创建小程序配置
     */
    @PostMapping("/config")
    @Operation(summary = "创建小程序配置")
    public Result<MiniProgramConfig> createConfig(@Valid @RequestBody MiniProgramConfigCreateRequest request) {
        MiniProgramConfig config = miniProgramConfigService.create(request);
        return Result.success(config);
    }
    
    /**
     * 更新小程序配置
     */
    @PutMapping("/config/{id}")
    @Operation(summary = "更新小程序配置")
    public Result<MiniProgramConfig> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody MiniProgramConfigUpdateRequest request) {
        MiniProgramConfig config = miniProgramConfigService.update(id, request);
        return Result.success(config);
    }
    
    /**
     * 根据配置键更新配置
     */
    @PutMapping("/config/key/{configKey}")
    @Operation(summary = "根据配置键更新配置")
    public Result<MiniProgramConfig> updateConfigByKey(
            @PathVariable String configKey,
            @Valid @RequestBody MiniProgramConfigUpdateRequest request) {
        MiniProgramConfig config = miniProgramConfigService.updateByConfigKey(configKey, request);
        return Result.success(config);
    }
    
    /**
     * 删除小程序配置
     */
    @DeleteMapping("/config/{id}")
    @Operation(summary = "删除小程序配置")
    public Result<?> deleteConfig(@PathVariable Long id) {
        miniProgramConfigService.delete(id);
        return Result.success();
    }
    
    /**
     * 根据配置键删除配置
     */
    @DeleteMapping("/config/key/{configKey}")
    @Operation(summary = "根据配置键删除配置")
    public Result<?> deleteConfigByKey(@PathVariable String configKey) {
        miniProgramConfigService.deleteByConfigKey(configKey);
        return Result.success();
    }
    
    /**
     * 获取小程序统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取小程序统计数据")
    public Result<com.travel.dto.MiniProgramStatisticsResponse> getStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        com.travel.dto.MiniProgramStatisticsResponse statistics = 
            miniProgramStatisticsService.getStatistics(startDate, endDate);
        return Result.success(statistics);
    }
}
