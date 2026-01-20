package com.travel.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 微信登录请求DTO
 * 
 * @author travel-platform
 */
@Data
public class WechatLoginRequest {
    
    /**
     * 微信登录code
     */
    @NotBlank(message = "code不能为空")
    private String code;
    
    /**
     * 加密的用户信息（通过wx.getUserProfile获取）
     * 可选，如果提供则会在登录时更新用户头像和昵称
     */
    private String encryptedData;
    
    /**
     * 初始向量（通过wx.getUserProfile获取）
     * 可选，与encryptedData一起使用
     */
    private String iv;
}
