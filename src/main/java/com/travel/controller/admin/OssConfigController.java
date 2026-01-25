package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.OssConfigRequest;
import com.travel.dto.OssConfigResponse;
import com.travel.entity.OssConfig;
import com.travel.service.OssConfigService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OSS配置管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/oss-config")
public class OssConfigController {
    
    @Autowired
    private OssConfigService ossConfigService;
    
    /**
     * 获取OSS配置
     * 
     * @return OSS配置信息
     */
    @GetMapping
    public Result<OssConfigResponse> getOssConfig() {
        try {
            OssConfig config = ossConfigService.getOssConfig();
            if (config == null) {
                return Result.success(null);
            }
            
            OssConfigResponse response = new OssConfigResponse();
            BeanUtils.copyProperties(config, response);
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取OSS配置失败", e);
            return Result.error("获取OSS配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 保存或更新OSS配置
     * 
     * @param request OSS配置请求
     * @return 保存后的配置信息
     */
    @PostMapping
    public Result<OssConfigResponse> saveOrUpdateOssConfig(@Valid @RequestBody OssConfigRequest request) {
        try {
            OssConfig config = new OssConfig();
            BeanUtils.copyProperties(request, config);
            
            // 如果AccessKeySecret为空或占位符，表示不修改密钥
            if (request.getAccessKeySecret() == null || 
                request.getAccessKeySecret().isEmpty() || 
                "********".equals(request.getAccessKeySecret())) {
                // 获取现有配置
                OssConfig existingConfig = ossConfigService.getOssConfig();
                if (existingConfig != null) {
                    config.setAccessKeySecret(existingConfig.getAccessKeySecret());
                }
            }
            
            OssConfig savedConfig = ossConfigService.saveOrUpdateOssConfig(config);
            
            OssConfigResponse response = new OssConfigResponse();
            BeanUtils.copyProperties(savedConfig, response);
            
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            log.warn("保存OSS配置失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("保存OSS配置异常", e);
            return Result.error("保存OSS配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 测试OSS连接
     * 
     * @param request OSS配置请求
     * @return 测试结果
     */
    @PostMapping("/test")
    public Result<String> testOssConnection(@Valid @RequestBody OssConfigRequest request) {
        try {
            OssConfig config = new OssConfig();
            BeanUtils.copyProperties(request, config);
            
            boolean success = ossConfigService.testOssConnection(config);
            if (success) {
                return Result.success("OSS连接测试成功");
            } else {
                return Result.error("OSS连接测试失败，请检查配置是否正确");
            }
        } catch (Exception e) {
            log.error("测试OSS连接异常", e);
            return Result.error("测试OSS连接失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除OSS配置
     * 
     * @return 删除结果
     */
    @DeleteMapping
    public Result<String> deleteOssConfig() {
        try {
            ossConfigService.deleteOssConfig();
            return Result.success("删除OSS配置成功");
        } catch (Exception e) {
            log.error("删除OSS配置异常", e);
            return Result.error("删除OSS配置失败：" + e.getMessage());
        }
    }
}
