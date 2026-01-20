package com.travel.controller.miniprogram;

import com.travel.common.Result;
import com.travel.common.ResultCode;
import com.travel.dto.LoginResponse;
import com.travel.dto.WechatLoginRequest;
import com.travel.entity.User;
import com.travel.exception.BusinessException;
import com.travel.service.UserAuthService;
import com.travel.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 小程序用户认证控制器
 * 使用微信session机制，不支持手机号验证码登录
 * 
 * @author travel-platform
 */
@Slf4j
@RestController("miniprogramAuthController")
@RequestMapping("/api/v1/miniprogram/auth")
@Tag(name = "小程序用户认证")
public class AuthController {
    
    @Autowired
    private UserAuthService userAuthService;
    
    /**
     * 微信登录
     * 仅支持微信授权登录
     */
    @PostMapping("/login")
    @Operation(summary = "微信登录")
    public Result<LoginResponse> login(@Valid @RequestBody WechatLoginRequest request) {
        if (!StringUtils.hasText(request.getCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "微信登录code不能为空");
        }
        
        LoginResponse response = userAuthService.wechatLogin(request);
        return Result.success(response);
    }
    
    /**
     * 登出
     * 清除session缓存
     */
    @PostMapping("/logout")
    @Operation(summary = "登出")
    public Result<?> logout(HttpServletRequest request) {
        // 从请求头或Cookie中获取session_id
        String sessionId = getSessionIdFromRequest(request);
        if (sessionId != null) {
            userAuthService.logout(sessionId);
        }
        return Result.success("登出成功");
    }
    
    /**
     * 刷新session
     * 重新获取code换取新的session_key
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新session")
    public Result<LoginResponse> refresh(@Valid @RequestBody WechatLoginRequest request) {
        if (!StringUtils.hasText(request.getCode())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "微信登录code不能为空");
        }
        
        // 重新登录，获取新的session_id
        LoginResponse response = userAuthService.wechatLogin(request);
        return Result.success(response);
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取用户信息")
    public Result<User> getUserInfo() {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        User user = userAuthService.getUserInfo(userId);
        return Result.success(user);
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/info")
    @Operation(summary = "更新用户信息")
    public Result<?> updateUserInfo(@RequestBody User userInfo) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        userAuthService.updateUserInfo(userId, userInfo);
        return Result.success("更新成功");
    }
    
    /**
     * 从请求中获取Session ID
     */
    private String getSessionIdFromRequest(HttpServletRequest request) {
        // 从请求头获取（X-Session-Id）
        String sessionId = request.getHeader("X-Session-Id");
        if (StringUtils.hasText(sessionId)) {
            return sessionId;
        }
        
        // 从Cookie获取
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("session_id".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
}
