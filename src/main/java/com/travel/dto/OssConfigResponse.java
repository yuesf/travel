package com.travel.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * OSS配置响应DTO
 * 
 * @author travel-platform
 */
@Data
public class OssConfigResponse {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * OSS Endpoint
     */
    private String endpoint;
    
    /**
     * Access Key ID
     */
    private String accessKeyId;
    
    /**
     * Access Key Secret（隐藏显示）
     */
    private String accessKeySecret;
    
    /**
     * Bucket名称
     */
    private String bucketName;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
