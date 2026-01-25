package com.travel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OSS配置属性类
 * 从application.yml或环境变量读取OSS配置
 * 
 * @author travel-platform
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "travel.oss")
public class OssProperties {
    
    /**
     * 是否启用OSS（默认：false）
     */
    private Boolean enabled = false;
    
    /**
     * OSS Endpoint（如：oss-cn-hangzhou.aliyuncs.com）
     */
    private String endpoint;
    
    /**
     * Access Key ID
     */
    private String accessKeyId;
    
    /**
     * Access Key Secret
     */
    private String accessKeySecret;
    
    /**
     * Bucket名称
     */
    private String bucketName;
    
    /**
     * 检查配置是否完整
     * @return true-配置完整，false-配置不完整
     */
    public boolean isConfigComplete() {
        return enabled != null && enabled 
            && endpoint != null && !endpoint.isEmpty()
            && accessKeyId != null && !accessKeyId.isEmpty()
            && accessKeySecret != null && !accessKeySecret.isEmpty()
            && bucketName != null && !bucketName.isEmpty();
    }
}
