package com.travel.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.travel.entity.OssConfig;
import com.travel.mapper.OssConfigMapper;
import com.travel.service.OssConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * OSS配置服务实现类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class OssConfigServiceImpl implements OssConfigService {
    
    @Autowired
    private OssConfigMapper ossConfigMapper;
    
    // AES加密密钥（16字节），生产环境应该从配置中心或环境变量获取
    private static final String AES_KEY = "TravelPlatform16"; // 16字节密钥
    
    @Override
    public OssConfig getOssConfig() {
        OssConfig config = ossConfigMapper.selectOne();
        if (config != null) {
            // 不返回加密后的密钥，前端显示时用占位符
            config.setAccessKeySecret("********");
        }
        return config;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OssConfig saveOrUpdateOssConfig(OssConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("配置对象不能为空");
        }
        
        // 验证必填字段
        if (config.getEndpoint() == null || config.getEndpoint().isEmpty()) {
            throw new IllegalArgumentException("Endpoint不能为空");
        }
        if (config.getAccessKeyId() == null || config.getAccessKeyId().isEmpty()) {
            throw new IllegalArgumentException("Access Key ID不能为空");
        }
        if (config.getAccessKeySecret() == null || config.getAccessKeySecret().isEmpty()) {
            throw new IllegalArgumentException("Access Key Secret不能为空");
        }
        if (config.getBucketName() == null || config.getBucketName().isEmpty()) {
            throw new IllegalArgumentException("Bucket名称不能为空");
        }
        
        // 加密AccessKeySecret
        String encryptedSecret = encryptAccessKeySecret(config.getAccessKeySecret());
        config.setAccessKeySecret(encryptedSecret);
        
        // 查询是否已存在配置
        OssConfig existingConfig = ossConfigMapper.selectOne();
        
        if (existingConfig == null) {
            // 新增配置
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            ossConfigMapper.insert(config);
            log.info("新增OSS配置成功，ID：{}", config.getId());
        } else {
            // 更新配置
            config.setId(existingConfig.getId());
            config.setUpdatedAt(LocalDateTime.now());
            ossConfigMapper.update(config);
            log.info("更新OSS配置成功，ID：{}", config.getId());
        }
        
        // 返回时隐藏密钥
        config.setAccessKeySecret("********");
        return config;
    }
    
    @Override
    public boolean testOssConnection(OssConfig config) {
        OSS ossClient = null;
        try {
            // 解密密钥（如果已加密）
            String accessKeySecret = config.getAccessKeySecret();
            if (!"********".equals(accessKeySecret)) {
                // 如果不是占位符，说明是新输入的明文密钥，需要先加密再解密测试
                accessKeySecret = decryptAccessKeySecret(encryptAccessKeySecret(accessKeySecret));
            } else {
                // 如果是占位符，从数据库获取加密后的密钥
                OssConfig existingConfig = ossConfigMapper.selectOne();
                if (existingConfig == null) {
                    throw new RuntimeException("OSS配置不存在");
                }
                accessKeySecret = decryptAccessKeySecret(existingConfig.getAccessKeySecret());
            }
            
            // 创建OSS客户端
            ossClient = new OSSClientBuilder().build(
                "https://" + config.getEndpoint(),
                config.getAccessKeyId(),
                accessKeySecret
            );
            
            // 测试连接：检查Bucket是否存在
            boolean exists = ossClient.doesBucketExist(config.getBucketName());
            if (!exists) {
                log.warn("OSS连接成功，但Bucket不存在：{}", config.getBucketName());
                return false;
            }
            
            log.info("OSS连接测试成功");
            return true;
            
        } catch (Exception e) {
            log.error("OSS连接测试失败", e);
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOssConfig() {
        ossConfigMapper.delete();
        log.info("删除OSS配置成功");
    }
    
    /**
     * 加密AccessKeySecret
     */
    private String encryptAccessKeySecret(String secret) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(secret.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("加密AccessKeySecret失败", e);
            throw new RuntimeException("加密AccessKeySecret失败", e);
        }
    }
    
    /**
     * 解密AccessKeySecret
     */
    private String decryptAccessKeySecret(String encryptedSecret) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedSecret));
            return new String(decrypted);
        } catch (Exception e) {
            log.error("解密AccessKeySecret失败", e);
            throw new RuntimeException("解密AccessKeySecret失败", e);
        }
    }
}
