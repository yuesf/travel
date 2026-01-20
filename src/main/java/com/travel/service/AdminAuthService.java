package com.travel.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.travel.common.ResultCode;
import com.travel.dto.LoginRequest;
import com.travel.dto.LoginResponse;
import com.travel.entity.AdminUser;
import com.travel.exception.BusinessException;
import com.travel.mapper.AdminUserMapper;
import com.travel.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 管理员认证服务
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class AdminAuthService {
    
    @Autowired
    private AdminUserMapper adminUserMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    @Qualifier("tokenBlacklistCache")
    private Cache<String, String> tokenBlacklistCache;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 管理员登录
     */
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        // 查询管理员
        AdminUser adminUser = adminUserMapper.selectByUsername(request.getUsername());
        if (adminUser == null) {
            // 尝试通过手机号查询
            adminUser = adminUserMapper.selectByPhone(request.getUsername());
        }
        
        if (adminUser == null) {
            throw new BusinessException(ResultCode.LOGIN_FAILED);
        }
        
        // 检查用户状态
        if (adminUser.getStatus() == null || adminUser.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), adminUser.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        
        // 生成Token（后台管理类型）
        String token = jwtUtil.generateToken(adminUser.getId(), adminUser.getUsername(), "admin");
        
        // 更新最后登录信息
        String clientIp = getClientIp(httpRequest);
        adminUserMapper.updateLastLoginInfo(adminUser.getId(), LocalDateTime.now(), clientIp);
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(adminUser.getId());
        response.setUsername(adminUser.getUsername());
        response.setRealName(adminUser.getRealName());
        
        log.info("管理员登录成功: username={}, userId={}", adminUser.getUsername(), adminUser.getId());
        
        return response;
    }
    
    /**
     * 管理员登出
     */
    public void logout(String token) {
        // 将Token加入黑名单
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token != null && jwtUtil.validateToken(token)) {
            // 将Token加入本地缓存黑名单（Caffeine会自动处理过期）
            tokenBlacklistCache.put(token, "1");
        }
        
        log.info("管理员登出成功");
    }
    
    /**
     * 检查Token是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null) {
            return false;
        }
        return tokenBlacklistCache.getIfPresent(token) != null;
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
