package com.travel.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 微信小程序用户信息解密工具类
 * 使用 AES-128-CBC 算法解密微信返回的加密用户信息
 * 
 * @author travel-platform
 */
@Slf4j
@Component
public class WechatDecryptUtil {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 解密微信用户信息
     * 
     * @param encryptedData 加密的用户信息
     * @param sessionKey 会话密钥
     * @param iv 初始向量
     * @return 解密后的用户信息JSON字符串
     * @throws Exception 解密失败时抛出异常
     */
    public String decrypt(String encryptedData, String sessionKey, String iv) throws Exception {
        try {
            // Base64解码
            byte[] dataByte = Base64.getDecoder().decode(encryptedData);
            byte[] keyByte = Base64.getDecoder().decode(sessionKey);
            byte[] ivByte = Base64.getDecoder().decode(iv);
            
            // 创建AES密钥
            SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
            
            // 创建Cipher实例，使用AES/CBC/PKCS5Padding
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            
            // 初始化Cipher，使用解密模式
            IvParameterSpec ivSpec = new IvParameterSpec(ivByte);
            cipher.init(Cipher.DECRYPT_MODE, spec, ivSpec);
            
            // 解密
            byte[] decrypted = cipher.doFinal(dataByte);
            
            // 转换为字符串
            String result = new String(decrypted, StandardCharsets.UTF_8);
            
            log.debug("微信用户信息解密成功");
            return result;
            
        } catch (Exception e) {
            log.error("微信用户信息解密失败: encryptedData={}, error={}", encryptedData, e.getMessage());
            throw new Exception("解密用户信息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解密并解析微信用户信息
     * 
     * @param encryptedData 加密的用户信息
     * @param sessionKey 会话密钥
     * @param iv 初始向量
     * @return 解析后的用户信息对象
     * @throws Exception 解密或解析失败时抛出异常
     */
    public WechatUserInfo decryptUserInfo(String encryptedData, String sessionKey, String iv) throws Exception {
        String decryptedJson = decrypt(encryptedData, sessionKey, iv);
        
        try {
            JsonNode jsonNode = objectMapper.readTree(decryptedJson);
            
            WechatUserInfo userInfo = new WechatUserInfo();
            if (jsonNode.has("nickName")) {
                userInfo.setNickname(jsonNode.get("nickName").asText());
            }
            if (jsonNode.has("avatarUrl")) {
                userInfo.setAvatar(jsonNode.get("avatarUrl").asText());
            }
            if (jsonNode.has("gender")) {
                userInfo.setGender(jsonNode.get("gender").asInt());
            }
            if (jsonNode.has("country")) {
                userInfo.setCountry(jsonNode.get("country").asText());
            }
            if (jsonNode.has("province")) {
                userInfo.setProvince(jsonNode.get("province").asText());
            }
            if (jsonNode.has("city")) {
                userInfo.setCity(jsonNode.get("city").asText());
            }
            if (jsonNode.has("language")) {
                userInfo.setLanguage(jsonNode.get("language").asText());
            }
            
            log.info("成功解析微信用户信息: nickname={}, avatar={}", userInfo.getNickname(), userInfo.getAvatar());
            return userInfo;
            
        } catch (Exception e) {
            log.error("解析微信用户信息失败: decryptedJson={}, error={}", decryptedJson, e.getMessage());
            throw new Exception("解析用户信息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 微信用户信息实体类
     */
    public static class WechatUserInfo {
        private String nickname;
        private String avatar;
        private Integer gender;
        private String country;
        private String province;
        private String city;
        private String language;
        
        public String getNickname() {
            return nickname;
        }
        
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
        
        public String getAvatar() {
            return avatar;
        }
        
        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
        
        public Integer getGender() {
            return gender;
        }
        
        public void setGender(Integer gender) {
            this.gender = gender;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        public String getProvince() {
            return province;
        }
        
        public void setProvince(String province) {
            this.province = province;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
        
        public String getLanguage() {
            return language;
        }
        
        public void setLanguage(String language) {
            this.language = language;
        }
    }
}
