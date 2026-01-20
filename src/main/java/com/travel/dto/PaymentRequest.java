package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 支付请求DTO
 * 
 * @author travel-platform
 */
@Data
public class PaymentRequest {
    
    /**
     * 支付方式：WECHAT-微信支付
     */
    @NotBlank(message = "支付方式不能为空")
    private String payType;
}
