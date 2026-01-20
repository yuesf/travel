package com.travel.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * Session信息
 * 存储小程序用户的session数据
 * 
 * @author travel-platform
 */
@Data
public class SessionInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 微信openid
     */
    private String openid;
    
    /**
     * 微信session_key（仅存储在服务器端）
     */
    private String sessionKey;
    
    /**
     * 微信unionid（可选）
     */
    private String unionid;
}
