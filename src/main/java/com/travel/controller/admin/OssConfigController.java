package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.config.OssProperties;
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
 * 注意：如果配置了启动参数（application.yml或环境变量），则优先使用启动参数配置
 * 数据库配置仅作为降级方案（向后兼容）
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/oss-config")
public class OssConfigController {
    
    @Autowired
    private OssConfigService ossConfigService;
    
    @Autowired
    private OssProperties ossProperties;
    
    /**
     * 获取OSS配置
     * 优先返回启动参数配置，如果未配置则返回数据库配置
     * 
     * @return OSS配置信息
     */
    @GetMapping
    public Result<OssConfigResponse> getOssConfig() {
        try {
            OssConfigResponse response = new OssConfigResponse();
            
            // 优先从启动参数读取配置
            if (ossProperties != null && ossProperties.isConfigComplete()) {
                response.setEndpoint(ossProperties.getEndpoint());
                response.setAccessKeyId(ossProperties.getAccessKeyId());
                response.setAccessKeySecret("********"); // 隐藏密钥
                response.setBucketName(ossProperties.getBucketName());
                response.setEnabled(ossProperties.getEnabled());
                log.debug("返回启动参数中的OSS配置");
                return Result.success(response);
            }
            
            // 如果启动参数未配置，从数据库读取（向后兼容）
            OssConfig config = ossConfigService.getOssConfig();
            if (config == null) {
                return Result.success(null);
            }
            
            BeanUtils.copyProperties(config, response);
            log.debug("返回数据库中的OSS配置（向后兼容）");
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取OSS配置失败", e);
            return Result.error("获取OSS配置失败：" + e.getMessage());
        }
    }
    
    /**
     * 保存或更新OSS配置
     * 注意：如果已配置启动参数，此接口将返回提示信息，建议通过启动参数配置
     * 
     * @param request OSS配置请求
     * @return 保存后的配置信息
     */
    @PostMapping
    public Result<OssConfigResponse> saveOrUpdateOssConfig(@Valid @RequestBody OssConfigRequest request) {
        try {
            // 检查是否已配置启动参数
            if (ossProperties != null && ossProperties.isConfigComplete()) {
                log.warn("已配置启动参数，数据库配置将被忽略。建议通过启动参数（application.yml或环境变量）配置OSS");
                return Result.error("已配置启动参数，请通过启动参数（application.yml或环境变量）配置OSS，数据库配置将被忽略");
            }
            
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
