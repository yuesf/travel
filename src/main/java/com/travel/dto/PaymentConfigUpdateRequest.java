package com.travel.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新支付配置请求DTO
 * 
 * @author travel-platform
 */
@Data
public class PaymentConfigUpdateRequest {
    
    /**
     * 小程序AppID
     */
    @Pattern(regexp = "^wx[a-zA-Z0-9]+$", message = "AppID格式不正确，必须以wx开头")
    private String appId;
    
    /**
     * 商户号
     */
    private String mchId;
    
    /**
     * API密钥（如果以****开头，则不更新）
     */
    private String apiKey;
    
    /**
     * 支付回调地址
     */
    private String notifyUrl;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 描述
     */
    private String description;
}
