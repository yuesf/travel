package com.travel.dto;

import lombok.Data;
import org.springframework.util.StringUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 统一登录请求DTO
 * 支持微信登录和手机号登录
 * 
 * @author travel-platform
 */
@Data
public class UnifiedLoginRequest {
    
    /**
     * 登录类型：WECHAT-微信登录，PHONE-手机号登录
     */
    @NotBlank(message = "登录类型不能为空")
    private String type;
    
    /**
     * 微信登录code（type为WECHAT时必填）
     * 手机号登录验证码（type为PHONE时，小程序使用code字段）
     */
    private String code;
    
    /**
     * 手机号（type为PHONE时必填）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确", groups = PhoneLogin.class)
    private String phone;
    
    /**
     * 验证码（type为PHONE时必填，兼容verifyCode字段名）
     */
    private String verifyCode;
    
    /**
     * 获取验证码（优先使用code，如果没有则使用verifyCode）
     * 用于手机号登录
     */
    public String getPhoneVerifyCode() {
        return StringUtils.hasText(code) ? code : verifyCode;
    }
    
    /**
     * 手机号登录验证组
     */
    public interface PhoneLogin {}
}
