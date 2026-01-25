package com.travel.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
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
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class OssServiceImpl implements OssService {
    
    @Autowired
    private OssConfigMapper ossConfigMapper;
    
    // AES加密密钥（16字节），生产环境应该从配置中心或环境变量获取
    private static final String AES_KEY = "TravelPlatform16"; // 16字节密钥
    
    /**
     * 获取OSS配置
     */
    private OssConfig getOssConfig() {
        return ossConfigMapper.selectOne();
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
    
    /**
     * 创建OSS客户端
     */
    private OSS createOssClient() {
        OssConfig config = getOssConfig();
        if (config == null || !config.getEnabled()) {
            throw new RuntimeException("OSS未配置或未启用");
        }
        
        String accessKeySecret = decryptAccessKeySecret(config.getAccessKeySecret());
        return new OSSClientBuilder().build(
            "https://" + config.getEndpoint(),
            config.getAccessKeyId(),
            accessKeySecret
        );
    }
    
    @Override
    public boolean isOssEnabled() {
        OssConfig config = getOssConfig();
        return config != null && config.getEnabled();
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
                OssConfig config = getOssConfig();
                if (config == null || !config.getEnabled()) {
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
                    config.getBucketName(),
                    filePath,
                    inputStream
                );
                ossClient.putObject(putObjectRequest);
                
                // 生成文件访问URL
                String fileUrl = String.format("https://%s.%s/%s",
                    config.getBucketName(),
                    config.getEndpoint(),
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
                OssConfig config = getOssConfig();
                if (config == null || !config.getEnabled()) {
                    throw new RuntimeException("OSS未配置或未启用");
                }
                
                ossClient = createOssClient();
                
                // 删除文件
                ossClient.deleteObject(config.getBucketName(), filePath);
                
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
            OssConfig config = getOssConfig();
            if (config == null || !config.getEnabled()) {
                return false;
            }
            
            ossClient = createOssClient();
            return ossClient.doesObjectExist(config.getBucketName(), filePath);
            
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
        OssConfig config = getOssConfig();
        if (config == null || !config.getEnabled()) {
            throw new RuntimeException("OSS未配置或未启用");
        }
        
        return String.format("https://%s.%s/%s",
            config.getBucketName(),
            config.getEndpoint(),
            filePath
        );
    }
    
    @Override
    public String generateSignedUrl(String filePath, int expireSeconds) {
        OSS ossClient = null;
        try {
            OssConfig config = getOssConfig();
            if (config == null || !config.getEnabled()) {
                throw new RuntimeException("OSS未配置或未启用");
            }
            
            ossClient = createOssClient();
            
            // 设置URL过期时间
            java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + expireSeconds * 1000L);
            
            // 生成签名URL
            java.net.URL url = ossClient.generatePresignedUrl(config.getBucketName(), filePath, expiration);
            
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
        
        OssConfig config = getOssConfig();
        if (config == null) {
            log.warn("OSS配置不存在，返回原URL: {}", url);
            return url;
        }
        
        log.info("尝试为URL生成签名URL: {}", url);
        log.info("OSS配置 - Bucket: {}, Endpoint: {}", config.getBucketName(), config.getEndpoint());
        
        // 检查URL是否是OSS URL（格式：https://{bucket}.{endpoint}/{path}）
        String expectedPrefix = String.format("https://%s.%s/", config.getBucketName(), config.getEndpoint());
        log.info("期望的URL前缀: {}", expectedPrefix);
        
        if (!url.startsWith(expectedPrefix)) {
            // 尝试更灵活的匹配：检查是否包含bucket和endpoint（顺序可能不同）
            // 例如：https://bucket.endpoint/path 或 https://endpoint/bucket/path
            boolean isOssUrl = url.contains(config.getBucketName()) && url.contains(config.getEndpoint());
            if (!isOssUrl) {
                log.warn("URL不匹配OSS配置，返回原URL。URL: {}, Bucket: {}, Endpoint: {}", 
                    url, config.getBucketName(), config.getEndpoint());
                return url;
            }
            // 如果包含bucket和endpoint，尝试从URL中提取路径
            // 格式：https://{bucket}.{endpoint}/{path}
            int pathStartIndex = url.indexOf("/", url.indexOf("://") + 3);
            if (pathStartIndex > 0) {
                String possiblePath = url.substring(pathStartIndex + 1);
                // 检查路径是否以bucket开头（某些OSS配置可能这样）
                if (possiblePath.startsWith(config.getBucketName() + "/")) {
                    String filePath = possiblePath.substring(config.getBucketName().length() + 1);
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
