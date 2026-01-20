package com.travel.config;

import com.travel.entity.AdminUser;
import com.travel.entity.Role;
import com.travel.mapper.AdminUserMapper;
import com.travel.service.AdminAuthService;
import com.travel.service.RoleService;
import com.travel.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
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
import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证过滤器
 * 
 * @author travel-platform
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final AdminAuthService adminAuthService;
    private final AdminUserMapper adminUserMapper;
    private final RoleService roleService;
    
    public JwtAuthenticationFilter(JwtUtil jwtUtil, AdminAuthService adminAuthService, 
                                  AdminUserMapper adminUserMapper, RoleService roleService) {
        this.jwtUtil = jwtUtil;
        this.adminAuthService = adminAuthService;
        this.adminUserMapper = adminUserMapper;
        this.roleService = roleService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        // 处理后台管理接口路径和通用接口路径（文件上传等）
        String requestPath = request.getRequestURI();
        boolean isAdminPath = requestPath.startsWith("/api/v1/admin/");
        boolean isCommonPath = requestPath.startsWith("/api/v1/common/");
        
        if (!isAdminPath && !isCommonPath) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 跳过已允许匿名访问的接口
        if (requestPath.equals("/api/v1/admin/auth/login") || 
            requestPath.startsWith("/api/v1/admin/init/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = getTokenFromRequest(request);
        
        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            // 检查token类型，确保是后台管理token
            String tokenType = jwtUtil.getTokenType(token);
            if (tokenType != null && !"admin".equals(tokenType)) {
                log.warn("后台管理接口使用了非管理员token: path={}, tokenType={}", requestPath, tokenType);
                filterChain.doFilter(request, response);
                return;
            }
            
            // 检查Token是否在黑名单中
            if (adminAuthService.isTokenBlacklisted(token)) {
                log.warn("Token已失效: {}", token);
                filterChain.doFilter(request, response);
                return;
            }
            
            // 从Token中获取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            
            if (userId != null && username != null) {
                // 查询用户信息
                AdminUser adminUser = adminUserMapper.selectById(userId);
                
                if (adminUser != null && adminUser.getStatus() != null && adminUser.getStatus() == 1) {
                    // 查询角色和权限
                    Role role = roleService.getByUserId(adminUser.getId());
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    
                    if (role != null) {
                        // 添加角色
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
                        
                        // 添加权限
                        List<String> permissionCodes = roleService.getPermissionCodesByRoleId(role.getId());
                        for (String permissionCode : permissionCodes) {
                            authorities.add(new SimpleGrantedAuthority(permissionCode));
                        }
                    } else {
                        // 默认角色
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    }
                    
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            adminUser,
                            null,
                            authorities
                        );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置到Security上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("后台管理员认证成功: userId={}, username={}, path={}", userId, username, requestPath);
                } else {
                    log.warn("后台管理员不存在或已禁用: userId={}", userId);
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
