package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 同步配置实体类
 * 
 * @author travel-platform
 */
@Data
public class SyncConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 同步类型：ATTRACTION-景点，HOTEL-酒店
     */
    private String syncType;
    
    /**
     * API地址
     */
    private String apiUrl;
    
    /**
     * API密钥
     */
    private String apiKey;
    
    /**
     * API密钥
     */
    private String apiSecret;
    
    /**
     * 同步频率：MANUAL-手动，DAILY-每日，WEEKLY-每周
     */
    private String syncFrequency;
    
    /**
     * 同步时间（如：02:00）
     */
    private String syncTime;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 最后同步时间
     */
    private LocalDateTime lastSyncTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
