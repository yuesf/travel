package com.travel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.config.WechatConfig;
import com.travel.exception.BusinessException;
import com.travel.common.ResultCode;
import com.travel.util.WechatDecryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

/**
 * 微信服务
 * 调用微信API
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class WechatService {
    
    @Autowired
    private WechatConfig wechatConfig;
    
    @Autowired(required = false)
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private WechatDecryptUtil wechatDecryptUtil;
    
    /**
     * 服务启动时检查微信配置
     * 如果配置缺失，则抛出异常阻止服务启动
     */
    @PostConstruct
    public void validateConfig() {
        if (wechatConfig.getAppid() == null || wechatConfig.getAppid().trim().isEmpty()) {
            String errorMsg = "微信小程序AppID未配置，请在配置文件中设置 miniprogram.wechat.appid 或环境变量 WECHAT_APPID";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        
        if (wechatConfig.getSecret() == null || wechatConfig.getSecret().trim().isEmpty()) {
            String errorMsg = "微信小程序AppSecret未配置，请在配置文件中设置 miniprogram.wechat.secret 或环境变量 WECHAT_SECRET";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        
        log.info("微信配置验证通过: appid={}", wechatConfig.getAppid());
    }
    
    /**
     * 通过code换取openid和session_key
     * 调用微信API: https://api.weixin.qq.com/sns/jscode2session
     * 
     * @param code 微信登录code
     * @return Code2SessionResponse 包含openid和session_key
     */
    public Code2SessionResponse code2Session(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(ResultCode.WECHAT_CODE_INVALID);
        }
        
        // 配置已在启动时验证，这里不再检查
        
        try {
            // 调用微信API
            String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wechatConfig.getAppid(), wechatConfig.getSecret(), code
            );
            
            // 使用RestTemplate调用API
            if (restTemplate == null) {
                restTemplate = new RestTemplate();
            }
            
            String responseBody = restTemplate.getForObject(url, String.class);
            log.debug("微信API响应: {}", responseBody);
            
            if (responseBody == null) {
                log.error("微信API返回空响应");
                throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
            }
            
            // 解析JSON响应
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            // 检查错误码
            if (jsonNode.has("errcode")) {
                int errcode = jsonNode.get("errcode").asInt();
                String errmsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "未知错误";
                log.error("微信API返回错误: errcode={}, errmsg={}", errcode, errmsg);
                throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED.getCode(), "微信登录失败: " + errmsg);
            }
            
            // 提取openid和session_key
            if (!jsonNode.has("openid")) {
                log.error("微信API响应中缺少openid: {}", responseBody);
                throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
            }
            
            Code2SessionResponse response = new Code2SessionResponse();
            response.setOpenid(jsonNode.get("openid").asText());
            if (jsonNode.has("session_key")) {
                response.setSessionKey(jsonNode.get("session_key").asText());
            }
            if (jsonNode.has("unionid")) {
                response.setUnionid(jsonNode.get("unionid").asText());
            }
            
            log.info("成功获取openid和session_key: openid={}", response.getOpenid());
            return response;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信API异常", e);
            throw new BusinessException(ResultCode.WECHAT_LOGIN_FAILED);
        }
    }
    
    /**
     * Code2Session响应
     */
    public static class Code2SessionResponse {
        private String openid;
        private String sessionKey;
        private String unionid;
        
        public String getOpenid() {
            return openid;
        }
        
        public void setOpenid(String openid) {
            this.openid = openid;
        }
        
        public String getSessionKey() {
            return sessionKey;
        }
        
        public void setSessionKey(String sessionKey) {
            this.sessionKey = sessionKey;
        }
        
        public String getUnionid() {
            return unionid;
        }
        
        public void setUnionid(String unionid) {
            this.unionid = unionid;
        }
    }
    
    /**
     * 解密微信用户信息
     * 
     * @param encryptedData 加密的用户信息
     * @param sessionKey 会话密钥
     * @param iv 初始向量
     * @return 解密后的用户信息
     */
    public WechatDecryptUtil.WechatUserInfo decryptUserInfo(String encryptedData, String sessionKey, String iv) {
        if (encryptedData == null || encryptedData.trim().isEmpty()) {
            log.warn("encryptedData为空，跳过解密");
            return null;
        }
        if (sessionKey == null || sessionKey.trim().isEmpty()) {
            log.warn("sessionKey为空，无法解密用户信息");
            return null;
        }
        if (iv == null || iv.trim().isEmpty()) {
            log.warn("iv为空，无法解密用户信息");
            return null;
        }
        
        try {
            return wechatDecryptUtil.decryptUserInfo(encryptedData, sessionKey, iv);
        } catch (Exception e) {
            log.error("解密微信用户信息失败", e);
            return null;
        }
    }
}
