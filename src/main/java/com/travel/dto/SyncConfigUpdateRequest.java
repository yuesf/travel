package com.travel.dto;

import lombok.Data;

/**
 * 更新同步配置请求DTO
 * 
 * @author travel-platform
 */
@Data
public class SyncConfigUpdateRequest {
    
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
}
