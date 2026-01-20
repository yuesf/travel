package com.travel.config;

import com.travel.entity.User;
import com.travel.mapper.UserMapper;
import com.travel.util.JwtUtil;
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
 * 小程序JWT认证过滤器
 * 专门处理小程序用户的鉴权
 * 
 * @author travel-platform
 */
@Slf4j
public class MiniprogramJwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    
    public MiniprogramJwtAuthenticationFilter(JwtUtil jwtUtil, UserMapper userMapper) {
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        // 处理小程序接口路径和通用接口路径（文件上传等）
        String requestPath = request.getRequestURI();
        boolean isMiniprogramPath = requestPath.startsWith("/api/v1/miniprogram/");
        boolean isCommonPath = requestPath.startsWith("/api/v1/common/");
        
        if (!isMiniprogramPath && !isCommonPath) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 跳过已允许匿名访问的接口
        if (requestPath.startsWith("/api/v1/miniprogram/auth/") || 
            requestPath.startsWith("/api/v1/miniprogram/home/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 对于通用接口路径，如果已经有认证信息（可能是admin token），则跳过
        if (isCommonPath && SecurityContextHolder.getContext().getAuthentication() != null 
            && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = getTokenFromRequest(request);
        
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            // 检查token类型，确保是小程序token
            String tokenType = jwtUtil.getTokenType(token);
            if (tokenType != null && !"miniprogram".equals(tokenType)) {
                log.warn("小程序接口使用了非小程序token: path={}, tokenType={}", requestPath, tokenType);
                filterChain.doFilter(request, response);
                return;
            }
            
            // 从Token中获取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            if (userId != null) {
                // 查询小程序用户信息
                User user = userMapper.selectById(userId);
                
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
                    
                    log.debug("小程序用户认证成功: userId={}, path={}", userId, requestPath);
                } else {
                    log.warn("小程序用户不存在或已禁用: userId={}", userId);
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
