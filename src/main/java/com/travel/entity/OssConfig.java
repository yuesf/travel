package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OSS配置实体类
 * 
 * @author travel-platform
 */
@Data
public class OssConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * OSS Endpoint（如：oss-cn-hangzhou.aliyuncs.com）
     */
    private String endpoint;
    
    /**
     * Access Key ID
     */
    private String accessKeyId;
    
    /**
     * Access Key Secret（加密存储）
     */
    private String accessKeySecret;
    
    /**
     * Bucket名称
     */
    private String bucketName;
    
    /**
     * 是否启用（1:启用, 0:禁用）
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
    
    /**
     * 创建人ID
     */
    private Long createdBy;
    
    /**
     * 更新人ID
     */
    private Long updatedBy;
}
