package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建同步配置请求DTO
 * 
 * @author travel-platform
 */
@Data
public class SyncConfigCreateRequest {
    
    /**
     * 同步类型：ATTRACTION-景点，HOTEL-酒店
     */
    @NotBlank(message = "同步类型不能为空")
    private String syncType;
    
    /**
     * API地址
     */
    @NotBlank(message = "API地址不能为空")
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
