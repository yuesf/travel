package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.PaymentConfigCreateRequest;
import com.travel.dto.PaymentConfigUpdateRequest;
import com.travel.entity.PaymentConfig;
import com.travel.service.PaymentConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 支付配置管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/payment-config")
@Tag(name = "支付配置管理")
public class PaymentConfigController {
    
    @Autowired
    private PaymentConfigService paymentConfigService;
    
    /**
     * 获取启用的支付配置
     */
    @GetMapping
    @Operation(summary = "获取启用的支付配置")
    public Result<PaymentConfig> get() {
        PaymentConfig config = paymentConfigService.getEnabledForDisplay();
        if (config == null) {
            return Result.error(com.travel.common.ResultCode.NOT_FOUND.getCode(), "支付配置不存在");
        }
        return Result.success(config);
    }
    
    /**
     * 根据ID获取支付配置
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取支付配置")
    public Result<PaymentConfig> getById(@PathVariable Long id) {
        PaymentConfig config = paymentConfigService.getForDisplay(id);
        return Result.success(config);
    }
    
    /**
     * 创建支付配置
     */
    @PostMapping
    @Operation(summary = "创建支付配置")
    public Result<PaymentConfig> create(@Valid @RequestBody PaymentConfigCreateRequest request) {
        PaymentConfig config = paymentConfigService.create(request);
        // 返回时隐藏API密钥
        config = paymentConfigService.getForDisplay(config.getId());
        return Result.success(config);
    }
    
    /**
     * 更新支付配置
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新支付配置")
    public Result<PaymentConfig> update(@PathVariable Long id, @Valid @RequestBody PaymentConfigUpdateRequest request) {
        PaymentConfig config = paymentConfigService.update(id, request);
        // 返回时隐藏API密钥
        config = paymentConfigService.getForDisplay(config.getId());
        return Result.success(config);
    }
    
    /**
     * 更新支付配置状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新支付配置状态")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        paymentConfigService.updateStatus(id, status);
        return Result.success();
    }
}
