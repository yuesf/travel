package com.travel.config;

import com.travel.dto.SessionInfo;
import com.travel.entity.User;
import com.travel.mapper.UserMapper;
import com.travel.service.UserAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * 小程序Session认证过滤器
 * 专门处理小程序用户的Session认证
 * 使用session_id而非JWT Token
 * 
 * @author travel-platform
 */
@Slf4j
public class MiniprogramSessionAuthenticationFilter extends OncePerRequestFilter {
    
    private final UserAuthService userAuthService;
    private final UserMapper userMapper;
    private final WechatConfig wechatConfig;
    
    public MiniprogramSessionAuthenticationFilter(UserAuthService userAuthService, 
                                                  UserMapper userMapper,
                                                  WechatConfig wechatConfig) {
        this.userAuthService = userAuthService;
        this.userMapper = userMapper;
        this.wechatConfig = wechatConfig;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        // 只处理小程序接口路径
        String requestPath = request.getRequestURI();
        if (!requestPath.startsWith("/api/v1/miniprogram/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 跳过已允许匿名访问的接口
        // 注意：/api/v1/miniprogram/auth/info 和 /api/v1/miniprogram/auth/update 需要认证，不能跳过
        if (requestPath.equals("/api/v1/miniprogram/auth/login") ||
            requestPath.equals("/api/v1/miniprogram/auth/logout") ||
            requestPath.equals("/api/v1/miniprogram/auth/refresh") ||
            requestPath.startsWith("/api/v1/miniprogram/home/") ||
            requestPath.startsWith("/api/v1/miniprogram/config/") ||
            requestPath.equals("/api/v1/miniprogram/categories") ||
            requestPath.startsWith("/api/v1/miniprogram/attractions") ||
            requestPath.startsWith("/api/v1/miniprogram/hotels") ||
            requestPath.startsWith("/api/v1/miniprogram/products") ||
            requestPath.startsWith("/api/v1/miniprogram/articles")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 从请求头或Cookie中获取session_id
        String sessionId = getSessionIdFromRequest(request);
        
        if (StringUtils.hasText(sessionId)) {
            // 从缓存中获取session信息
            SessionInfo sessionInfo = userAuthService.getSessionInfo(sessionId);
            
            if (sessionInfo != null && sessionInfo.getUserId() != null) {
                // 查询用户信息
                User user = userMapper.selectById(sessionInfo.getUserId());
                
                if (user != null && user.getStatus() != null && user.getStatus() == 1) {
                    // 小程序用户使用简单的角色标识
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_MINIPROGRAM_USER"))
                        );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("小程序用户Session认证成功: userId={}, sessionId={}, path={}", 
                             user.getId(), sessionId, requestPath);
                } else {
                    log.warn("小程序用户不存在或已禁用: userId={}", sessionInfo.getUserId());
                }
            } else {
                log.debug("Session无效或已过期: sessionId={}", sessionId);
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求中获取Session ID
     * 优先从请求头获取，其次从Cookie获取
     */
    private String getSessionIdFromRequest(HttpServletRequest request) {
        // 从请求头获取（X-Session-Id）
        String sessionId = request.getHeader(wechatConfig.getSession().getHeader());
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
