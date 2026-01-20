package com.travel.util;

import com.travel.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security工具类
 * 用于从Security上下文获取当前认证用户信息
 * 
 * @author travel-platform
 */
public class SecurityUtils {
    
    /**
     * 获取当前小程序用户
     */
    public static User getCurrentMiniprogramUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * 获取当前小程序用户ID
     */
    public static Long getCurrentMiniprogramUserId() {
        User user = getCurrentMiniprogramUser();
        return user != null ? user.getId() : null;
    }
    
    /**
     * 检查当前是否为小程序用户
     */
    public static boolean isMiniprogramUser() {
        return getCurrentMiniprogramUser() != null;
    }
}
