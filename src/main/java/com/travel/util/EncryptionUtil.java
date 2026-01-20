package com.travel.util;

import lombok.extern.slf4j.Slf4j;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 加密工具类
 * 
 * @author travel-platform
 */
@Slf4j
public class EncryptionUtil {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    /**
     * 获取加密密钥（从环境变量读取，如果不存在则使用默认密钥）
     */
    private static String getSecretKey() {
        String key = System.getenv("ENCRYPTION_KEY");
        if (key == null || key.isEmpty()) {
            // 默认密钥（生产环境必须从环境变量读取）
            key = "travel-platform-encryption-key-2024";
            log.warn("使用默认加密密钥，生产环境请设置ENCRYPTION_KEY环境变量");
        }
        // AES密钥必须是16、24或32字节
        if (key.length() < 16) {
            key = String.format("%-16s", key).substring(0, 16);
        } else if (key.length() > 32) {
            key = key.substring(0, 32);
        } else if (key.length() < 24) {
            key = String.format("%-24s", key).substring(0, 24);
        } else if (key.length() < 32) {
            key = String.format("%-32s", key).substring(0, 32);
        }
        return key;
    }
    
    /**
     * 加密
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(getSecretKey().getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("加密失败", e);
            throw new RuntimeException("加密失败", e);
        }
    }
    
    /**
     * 解密
     */
    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            SecretKeySpec secretKey = new SecretKeySpec(getSecretKey().getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("解密失败", e);
            throw new RuntimeException("解密失败", e);
        }
    }
    
    /**
     * 部分隐藏字符串（用于显示）
     */
    public static String maskString(String str, int visibleLength) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.length() <= visibleLength) {
            return "****";
        }
        return "****" + str.substring(str.length() - visibleLength);
    }
}
