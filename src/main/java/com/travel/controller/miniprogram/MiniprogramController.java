package com.travel.controller.miniprogram;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.Result;
import com.travel.entity.MiniProgramConfig;
import com.travel.service.MiniProgramConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 小程序公共配置控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/miniprogram/config")
@Tag(name = "小程序配置")
public class MiniprogramController {
    
    @Autowired
    private MiniProgramConfigService miniProgramConfigService;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 获取小程序 Logo 配置
     * 无需认证，logo 是公开信息
     */
    @GetMapping("/logo")
    @Operation(summary = "获取小程序 Logo 配置")
    public Result<Map<String, String>> getLogoConfig() {
        try {
            MiniProgramConfig config = miniProgramConfigService.getByConfigKey("MINIPROGRAM_LOGO");
            String logoUrl = null;
            
            if (config != null && config.getStatus() == 1 && config.getConfigValue() != null) {
                try {
                    Map<String, Object> configValue = objectMapper.readValue(
                        config.getConfigValue(), 
                        new TypeReference<Map<String, Object>>() {}
                    );
                    logoUrl = (String) configValue.get("logoUrl");
                } catch (Exception e) {
                    log.warn("解析 Logo 配置失败，配置键: MINIPROGRAM_LOGO", e);
                }
            }
            
            Map<String, String> result = new HashMap<>();
            result.put("logoUrl", logoUrl);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取 Logo 配置失败", e);
            // 发生异常时返回 null，不抛出异常，保证接口可用性
            Map<String, String> result = new HashMap<>();
            result.put("logoUrl", null);
            return Result.success(result);
        }
    }
}
