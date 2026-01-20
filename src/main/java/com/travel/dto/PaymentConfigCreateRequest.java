package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 创建支付配置请求DTO
 * 
 * @author travel-platform
 */
@Data
public class PaymentConfigCreateRequest {
    
    /**
     * 小程序AppID
     */
    @NotBlank(message = "AppID不能为空")
    @Pattern(regexp = "^wx[a-zA-Z0-9]+$", message = "AppID格式不正确，必须以wx开头")
    private String appId;
    
    /**
     * 商户号
     */
    @NotBlank(message = "商户号不能为空")
    private String mchId;
    
    /**
     * API密钥
     */
    @NotBlank(message = "API密钥不能为空")
    private String apiKey;
    
    /**
     * 支付回调地址
     */
    @NotBlank(message = "支付回调地址不能为空")
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
