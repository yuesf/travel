package com.travel.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 登录响应DTO
 * 
 * @author travel-platform
 */
@Data
public class LoginResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Token（后台管理系统使用）
     */
    private String token;
    
    /**
     * Session ID（小程序使用）
     */
    private String sessionId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 用户信息（小程序使用）
     */
    private UserInfo userInfo;
    
    @Data
    public static class UserInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String nickname;
        private String avatar;
        private String phone;
        private Integer gender;
    }
}
