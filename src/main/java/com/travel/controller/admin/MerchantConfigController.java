package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.MerchantConfigCreateRequest;
import com.travel.dto.MerchantConfigUpdateRequest;
import com.travel.entity.MerchantConfig;
import com.travel.service.MerchantConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 商家配置管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/merchant-config")
@Tag(name = "商家配置管理")
public class MerchantConfigController {
    
    @Autowired
    private MerchantConfigService merchantConfigService;
    
    /**
     * 获取商家配置
     */
    @GetMapping
    @Operation(summary = "获取商家配置")
    public Result<MerchantConfig> get() {
        MerchantConfig config = merchantConfigService.get();
        if (config == null) {
            return Result.error(com.travel.common.ResultCode.NOT_FOUND.getCode(), "商家配置不存在");
        }
        return Result.success(config);
    }
    
    /**
     * 创建商家配置
     */
    @PostMapping
    @Operation(summary = "创建商家配置")
    public Result<MerchantConfig> create(@Valid @RequestBody MerchantConfigCreateRequest request) {
        MerchantConfig config = merchantConfigService.create(request);
        return Result.success(config);
    }
    
    /**
     * 更新商家配置
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新商家配置")
    public Result<MerchantConfig> update(@PathVariable Long id, @Valid @RequestBody MerchantConfigUpdateRequest request) {
        MerchantConfig config = merchantConfigService.update(id, request);
        return Result.success(config);
    }
    
    /**
     * 更新商家配置（使用现有配置ID）
     */
    @PutMapping
    @Operation(summary = "更新商家配置")
    public Result<MerchantConfig> update(@Valid @RequestBody MerchantConfigUpdateRequest request) {
        MerchantConfig config = merchantConfigService.update(request);
        return Result.success(config);
    }
}
