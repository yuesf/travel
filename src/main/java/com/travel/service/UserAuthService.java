package com.travel.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.travel.common.ResultCode;
import com.travel.dto.LoginResponse;
import com.travel.dto.SessionInfo;
import com.travel.dto.WechatLoginRequest;
import com.travel.entity.User;
import com.travel.exception.BusinessException;
import com.travel.mapper.UserMapper;
import com.travel.service.WechatService.Code2SessionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

/**
 * 小程序用户认证服务
 * 使用微信session机制，不使用JWT Token
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class UserAuthService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private WechatService wechatService;
    
    @Autowired
    @Qualifier("miniprogramSessionCache")
    private Cache<String, SessionInfo> sessionCache;
    
    /**
     * 微信登录
     * 使用微信session机制，生成session_id
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse wechatLogin(WechatLoginRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new BusinessException(ResultCode.WECHAT_CODE_INVALID);
        }
        
        // 调用微信API通过code换取openid和session_key
        Code2SessionResponse wechatResponse = wechatService.code2Session(request.getCode());
        
        if (wechatResponse.getOpenid() == null) {
            log.error("微信登录失败: 无法获取openid, code={}", request.getCode());
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
        }
        
        String openid = wechatResponse.getOpenid();
        String sessionKey = wechatResponse.getSessionKey();
        String unionid = wechatResponse.getUnionid();
        
        // 查询或创建用户
        User user = userMapper.selectByOpenid(openid);
        if (user == null) {
            // 创建新用户
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(unionid);
            user.setIsFirstOrder(1);
            user.setStatus(1);
            
            // 生成默认昵称：用户+6位随机数字
            String defaultNickname = generateDefaultNickname();
            user.setNickname(defaultNickname);
            
            userMapper.insert(user);
            log.info("创建新用户: openid={}, userId={}, nickname={}", 
                openid, user.getId(), user.getNickname());
        } else {
            // 检查用户状态
            if (user.getStatus() == null || user.getStatus() == 0) {
                throw new BusinessException(ResultCode.USER_DISABLED);
            }
            
            // 更新unionid（如果之前没有）
            if (unionid != null && (user.getUnionid() == null || user.getUnionid().isEmpty())) {
                user.setUnionid(unionid);
                userMapper.updateById(user);
            }
        }
        
        // 生成session_id
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        
        // 存储session信息到缓存（Key前缀：miniprogram:session:）
        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.setUserId(user.getId());
        sessionInfo.setOpenid(openid);
        sessionInfo.setSessionKey(sessionKey); // session_key仅存储在服务器端
        sessionInfo.setUnionid(unionid);
        
        String cacheKey = "miniprogram:session:" + sessionId;
        sessionCache.put(cacheKey, sessionInfo);
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setSessionId(sessionId); // 返回session_id，不返回session_key
        response.setUserId(user.getId());
        response.setUsername(user.getNickname() != null ? user.getNickname() : (user.getOpenid() != null ? user.getOpenid().substring(0, Math.min(8, user.getOpenid().length())) : "用户"));
        
        // 设置用户信息
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        // 如果昵称为空，使用username作为默认昵称
        userInfo.setNickname(user.getNickname() != null && !user.getNickname().trim().isEmpty() 
            ? user.getNickname() 
            : response.getUsername());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setPhone(user.getPhone());
        userInfo.setGender(user.getGender());
        response.setUserInfo(userInfo);
        
        log.info("用户微信登录成功: openid={}, userId={}, sessionId={}", openid, user.getId(), sessionId);
        
        return response;
    }
    
    /**
     * 登出
     * 清除session缓存
     */
    public void logout(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            String cacheKey = "miniprogram:session:" + sessionId;
            sessionCache.invalidate(cacheKey);
            log.info("用户登出: sessionId={}", sessionId);
        }
    }
    
    /**
     * 根据session_id获取session信息
     */
    public SessionInfo getSessionInfo(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }
        String cacheKey = "miniprogram:session:" + sessionId;
        return sessionCache.getIfPresent(cacheKey);
    }
    
    /**
     * 根据session_id获取用户信息
     */
    public User getUserBySessionId(String sessionId) {
        SessionInfo sessionInfo = getSessionInfo(sessionId);
        if (sessionInfo == null || sessionInfo.getUserId() == null) {
            return null;
        }
        return userMapper.selectById(sessionInfo.getUserId());
    }
    
    /**
     * 更新用户信息
     */
    public void updateUserInfo(Long userId, User userInfo) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        if (userInfo.getNickname() != null) {
            user.setNickname(userInfo.getNickname());
        }
        if (userInfo.getAvatar() != null) {
            user.setAvatar(userInfo.getAvatar());
        }
        if (userInfo.getGender() != null) {
            user.setGender(userInfo.getGender());
        }
        if (userInfo.getPhone() != null) {
            user.setPhone(userInfo.getPhone());
        }
        
        userMapper.updateById(user);
    }
    
    /**
     * 获取用户信息
     */
    public User getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }
    
    /**
     * 生成默认昵称：用户+6位随机数字
     * 
     * @return 默认昵称，例如：用户123456
     */
    private String generateDefaultNickname() {
        Random random = new Random();
        // 生成6位随机数字（100000-999999）
        int randomNum = 100000 + random.nextInt(900000);
        return "用户" + randomNum;
    }
}
