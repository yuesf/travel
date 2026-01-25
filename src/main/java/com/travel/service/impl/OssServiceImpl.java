package com.travel.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.travel.config.OssProperties;
import com.travel.entity.OssConfig;
import com.travel.mapper.OssConfigMapper;
import com.travel.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

/**
 * OSS文件服务实现类
 * 优先从启动参数（application.yml或环境变量）读取配置，如果未配置则从数据库读取（向后兼容）
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class OssServiceImpl implements OssService {
    
    @Autowired(required = false)
    private OssConfigMapper ossConfigMapper;
    
    @Autowired
    private OssProperties ossProperties;
    
    // AES加密密钥（16字节），用于解密数据库中的加密密钥（向后兼容）
    private static final String AES_KEY = "TravelPlatform16"; // 16字节密钥
    
    /**
     * OSS配置信息（内部类）
     */
    private static class OssConfigInfo {
        String endpoint;
        String accessKeyId;
        String accessKeySecret;
        String bucketName;
        boolean enabled;
        
        OssConfigInfo(String endpoint, String accessKeyId, String accessKeySecret, 
                     String bucketName, boolean enabled) {
            this.endpoint = endpoint;
            this.accessKeyId = accessKeyId;
            this.accessKeySecret = accessKeySecret;
            this.bucketName = bucketName;
            this.enabled = enabled;
        }
    }
    
    /**
     * 获取OSS配置（优先从启动参数读取，如果未配置则从数据库读取）
     */
    private OssConfigInfo getOssConfigInfo() {
        // 优先从启动参数读取
        if (ossProperties != null && ossProperties.isConfigComplete()) {
            log.debug("从启动参数读取OSS配置");
            return new OssConfigInfo(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret(),
                ossProperties.getBucketName(),
                ossProperties.getEnabled()
            );
        }
        
        // 如果启动参数未配置，尝试从数据库读取（向后兼容）
        if (ossConfigMapper != null) {
            OssConfig dbConfig = ossConfigMapper.selectOne();
            if (dbConfig != null && dbConfig.getEnabled()) {
                log.debug("从数据库读取OSS配置（向后兼容）");
                try {
                    String accessKeySecret = decryptAccessKeySecret(dbConfig.getAccessKeySecret());
                    return new OssConfigInfo(
                        dbConfig.getEndpoint(),
                        dbConfig.getAccessKeyId(),
                        accessKeySecret,
                        dbConfig.getBucketName(),
                        dbConfig.getEnabled()
                    );
                } catch (Exception e) {
                    log.warn("从数据库读取OSS配置失败，尝试解密密钥时出错", e);
                }
            }
        }
        
        return null;
    }
    
    /**
     * 解密AccessKeySecret（仅用于数据库配置，向后兼容）
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
    
    /**
     * 创建OSS客户端
     */
    private OSS createOssClient() {
        OssConfigInfo config = getOssConfigInfo();
        if (config == null || !config.enabled) {
            throw new RuntimeException("OSS未配置或未启用");
        }
        
        return new OSSClientBuilder().build(
            "https://" + config.endpoint,
            config.accessKeyId,
            config.accessKeySecret
        );
    }
    
    @Override
    public boolean isOssEnabled() {
        OssConfigInfo config = getOssConfigInfo();
        return config != null && config.enabled;
    }
    
    @Override
    public String uploadFile(MultipartFile file, String module) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        return uploadFile(file.getInputStream(), originalFilename, module);
    }
    
    @Override
    public String uploadFile(InputStream inputStream, String fileName, String module) throws Exception {
        OSS ossClient = null;
        int retryCount = 0;
        int maxRetries = 3;
        Exception lastException = null;
        
        while (retryCount < maxRetries) {
            try {
                OssConfigInfo config = getOssConfigInfo();
                if (config == null || !config.enabled) {
                    throw new RuntimeException("OSS未配置或未启用");
                }
                
                ossClient = createOssClient();
                
                // 生成文件路径：{module}/{type}/{date}/{uuid}.{extension}
                String fileExtension = getFileExtension(fileName);
                String fileType = getFileType(fileExtension);
                String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String uuid = UUID.randomUUID().toString().replace("-", "");
                String filePath = String.format("%s/%s/%s/%s.%s", module, fileType, dateStr, uuid, fileExtension);
                
                // 上传文件到OSS
                PutObjectRequest putObjectRequest = new PutObjectRequest(
                    config.bucketName,
                    filePath,
                    inputStream
                );
                ossClient.putObject(putObjectRequest);
                
                // 生成文件访问URL
                String fileUrl = String.format("https://%s.%s/%s",
                    config.bucketName,
                    config.endpoint,
                    filePath
                );
                
                log.info("文件上传成功：{}", fileUrl);
                return fileUrl;
                
            } catch (Exception e) {
                retryCount++;
                lastException = e;
                log.error("文件上传失败，重试次数：{}/{}", retryCount, maxRetries, e);
                
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("文件上传失败，已重试" + maxRetries + "次", lastException);
                }
                
                // 等待一段时间后重试
                Thread.sleep(1000 * retryCount);
                
            } finally {
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
        }
        
        throw new RuntimeException("文件上传失败", lastException);
    }
    
    @Override
    public boolean deleteFile(String filePath) throws Exception {
        OSS ossClient = null;
        int retryCount = 0;
        int maxRetries = 3;
        Exception lastException = null;
        
        while (retryCount < maxRetries) {
            try {
                OssConfigInfo config = getOssConfigInfo();
                if (config == null || !config.enabled) {
                    throw new RuntimeException("OSS未配置或未启用");
                }
                
                ossClient = createOssClient();
                
                // 删除文件
                ossClient.deleteObject(config.bucketName, filePath);
                
                log.info("文件删除成功：{}", filePath);
                return true;
                
            } catch (Exception e) {
                retryCount++;
                lastException = e;
                log.error("文件删除失败，重试次数：{}/{}", retryCount, maxRetries, e);
                
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("文件删除失败，已重试" + maxRetries + "次", lastException);
                }
                
                // 等待一段时间后重试
                Thread.sleep(1000 * retryCount);
                
            } finally {
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
        }
        
        throw new RuntimeException("文件删除失败", lastException);
    }
    
    @Override
    public boolean fileExists(String filePath) {
        OSS ossClient = null;
        try {
            OssConfigInfo config = getOssConfigInfo();
            if (config == null || !config.enabled) {
                return false;
            }
            
            ossClient = createOssClient();
            return ossClient.doesObjectExist(config.bucketName, filePath);
            
        } catch (Exception e) {
            log.error("检查文件是否存在失败：{}", filePath, e);
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    @Override
    public String getFileUrl(String filePath) {
        OssConfigInfo config = getOssConfigInfo();
        if (config == null || !config.enabled) {
            throw new RuntimeException("OSS未配置或未启用");
        }
        
        return String.format("https://%s.%s/%s",
            config.bucketName,
            config.endpoint,
            filePath
        );
    }
    
    @Override
    public String generateSignedUrl(String filePath, int expireSeconds) {
        OSS ossClient = null;
        try {
            OssConfigInfo config = getOssConfigInfo();
            if (config == null || !config.enabled) {
                throw new RuntimeException("OSS未配置或未启用");
            }
            
            ossClient = createOssClient();
            
            // 设置URL过期时间
            java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + expireSeconds * 1000L);
            
            // 生成签名URL
            java.net.URL url = ossClient.generatePresignedUrl(config.bucketName, filePath, expiration);
            
            String signedUrl = url.toString();
            log.debug("生成签名URL成功：{}", signedUrl);
            
            return signedUrl;
            
        } catch (Exception e) {
            log.error("生成签名URL失败：{}", filePath, e);
            throw new RuntimeException("生成签名URL失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * 根据文件扩展名判断文件类型
     */
    private String getFileType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return "other";
        }
        
        String ext = extension.toLowerCase();
        if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || 
            ext.equals("gif") || ext.equals("webp") || ext.equals("bmp")) {
            return "image";
        } else if (ext.equals("mp4") || ext.equals("avi") || ext.equals("mov") || 
                   ext.equals("wmv") || ext.equals("flv") || ext.equals("mkv")) {
            return "video";
        } else {
            return "other";
        }
    }
    
    @Override
    public String generateSignedUrlFromUrl(String url, int expireSeconds) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        
        // 检查OSS是否启用
        if (!isOssEnabled()) {
            log.info("OSS未启用，返回原URL: {}", url);
            return url;
        }
        
        OssConfigInfo config = getOssConfigInfo();
        if (config == null) {
            log.warn("OSS配置不存在，返回原URL: {}", url);
            return url;
        }
        
        log.info("尝试为URL生成签名URL: {}", url);
        log.info("OSS配置 - Bucket: {}, Endpoint: {}", config.bucketName, config.endpoint);
        
        // 检查URL是否是OSS URL（格式：https://{bucket}.{endpoint}/{path}）
        String expectedPrefix = String.format("https://%s.%s/", config.bucketName, config.endpoint);
        log.info("期望的URL前缀: {}", expectedPrefix);
        
        if (!url.startsWith(expectedPrefix)) {
            // 尝试更灵活的匹配：检查是否包含bucket和endpoint（顺序可能不同）
            // 例如：https://bucket.endpoint/path 或 https://endpoint/bucket/path
            boolean isOssUrl = url.contains(config.bucketName) && url.contains(config.endpoint);
            if (!isOssUrl) {
                log.warn("URL不匹配OSS配置，返回原URL。URL: {}, Bucket: {}, Endpoint: {}", 
                    url, config.bucketName, config.endpoint);
                return url;
            }
            // 如果包含bucket和endpoint，尝试从URL中提取路径
            // 格式：https://{bucket}.{endpoint}/{path}
            int pathStartIndex = url.indexOf("/", url.indexOf("://") + 3);
            if (pathStartIndex > 0) {
                String possiblePath = url.substring(pathStartIndex + 1);
                // 检查路径是否以bucket开头（某些OSS配置可能这样）
                if (possiblePath.startsWith(config.bucketName + "/")) {
                    String filePath = possiblePath.substring(config.bucketName.length() + 1);
                    log.info("从URL提取的文件路径: {}", filePath);
                    try {
                        return generateSignedUrl(filePath, expireSeconds);
                    } catch (Exception e) {
                        log.error("根据URL生成签名URL失败：{}", url, e);
                        return url;
                    }
                }
            }
            log.warn("无法从URL提取文件路径，返回原URL: {}", url);
            return url;
        }
        
        // 从URL中提取文件路径（相对路径）
        String filePath = url.substring(expectedPrefix.length());
        log.info("从URL提取的文件路径: {}", filePath);
        
        // 生成签名URL
        try {
            String signedUrl = generateSignedUrl(filePath, expireSeconds);
            log.info("生成签名URL成功: {}", signedUrl);
            return signedUrl;
        } catch (Exception e) {
            log.error("根据URL生成签名URL失败：{}", url, e);
            // 如果生成失败，返回原URL
            return url;
        }
    }
}
