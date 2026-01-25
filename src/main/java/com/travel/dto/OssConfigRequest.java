package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OSS配置请求DTO
 * 
 * @author travel-platform
 */
@Data
public class OssConfigRequest {
    
    /**
     * OSS Endpoint（如：oss-cn-hangzhou.aliyuncs.com）
     */
    @NotBlank(message = "Endpoint不能为空")
    private String endpoint;
    
    /**
     * Access Key ID
     */
    @NotBlank(message = "Access Key ID不能为空")
    private String accessKeyId;
    
    /**
     * Access Key Secret
     */
    private String accessKeySecret;
    
    /**
     * Bucket名称
     */
    @NotBlank(message = "Bucket名称不能为空")
    private String bucketName;
    
    /**
     * 是否启用（1:启用, 0:禁用）
     */
    private Boolean enabled;
}
