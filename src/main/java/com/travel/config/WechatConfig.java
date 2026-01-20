package com.travel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信小程序配置
 * 
 * @author travel-platform
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "miniprogram.wechat")
public class WechatConfig {
    
    /**
     * 小程序AppID
     */
    private String appid;
    
    /**
     * 小程序AppSecret
     */
    private String secret;
    
    /**
     * Session配置
     */
    private SessionConfig session = new SessionConfig();
    
    /**
     * 支付配置
     */
    private PayConfig pay = new PayConfig();
    
    @Data
    public static class SessionConfig {
        /**
         * Session过期时间（秒），默认30天
         */
        private Long expiration = 2592000L;
        
        /**
         * Session ID请求头名称
         */
        private String header = "X-Session-Id";
    }
    
    @Data
    public static class PayConfig {
        /**
         * 商户号
         */
        private String mchId;
        
        /**
         * API密钥
         */
        private String apiKey;
        
        /**
         * 支付回调地址
         */
        private String notifyUrl;
    }
}
