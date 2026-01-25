package com.travel.service;

import com.travel.entity.FileRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传服务（仅支持OSS存储）
 * 
 * 注意：已移除本地存储上传功能，所有文件必须上传到OSS。
 * 历史已存在的本地文件记录仍然保留，但不再支持新文件上传到本地存储。
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class FileService {
    
    @Autowired
    private OssService ossService;
    
    @Autowired
    private FileRecordService fileRecordService;
    
    @Autowired
    private ImageCompressionService imageCompressionService;
    
    /**
     * 上传图片（仅支持OSS存储）
     * 
     * @param file 图片文件
     * @param module 模块名称（如：banner, attraction等）
     * @return 文件访问URL
     * @throws IOException IO异常
     * @throws IllegalStateException OSS未配置时抛出
     */
    public String uploadImage(MultipartFile file, String module) throws IOException {
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只能上传图片文件");
        }
        
        // 验证文件大小（10MB）
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("图片大小不能超过10MB");
        }
        
        // 验证文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        String extension = getExtension(originalFilename);
        if (!isValidImageExtension(extension)) {
            throw new IllegalArgumentException("只支持JPG、JPEG、PNG、WebP格式的图片");
        }
        
        return uploadFile(file, module, "image");
    }
    
    /**
     * 上传视频（仅支持OSS存储）
     * 
     * @param file 视频文件
     * @param module 模块名称（如：banner, attraction等）
     * @return 文件访问URL
     * @throws IOException IO异常
     * @throws IllegalStateException OSS未配置时抛出
     */
    public String uploadVideo(MultipartFile file, String module) throws IOException {
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("只能上传视频文件");
        }
        
        // 验证文件大小（100MB）
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("视频大小不能超过100MB");
        }
        
        // 验证文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        String extension = getExtension(originalFilename);
        if (!isValidVideoExtension(extension)) {
            throw new IllegalArgumentException("只支持MP4格式的视频");
        }
        
        return uploadFile(file, module, "video");
    }
    
    /**
     * 上传文件通用方法（仅支持OSS存储）
     * 
     * @param file 文件
     * @param module 模块名称
     * @param type 文件类型（image或video）
     * @return 文件访问URL
     * @throws IOException IO异常
     * @throws IllegalStateException OSS未配置时抛出
     */
    private String uploadFile(MultipartFile file, String module, String type) throws IOException {
        // 检查OSS是否配置
        if (!ossService.isOssEnabled()) {
            String errorMsg = "OSS未配置，请先在系统配置中配置OSS信息";
            log.warn("OSS未配置，文件上传失败: {}", file.getOriginalFilename());
            throw new IllegalStateException(errorMsg);
        }
        
        MultipartFile fileToUpload = file;
        String originalFilename = file.getOriginalFilename();
        
        // 如果是图片，尝试压缩为 WebP 格式
        if ("image".equals(type)) {
            try {
                fileToUpload = imageCompressionService.compressToWebP(file);
                log.debug("图片压缩处理完成");
            } catch (Exception e) {
                log.warn("图片压缩失败，使用原始文件: {}", e.getMessage());
                // 压缩失败时使用原始文件
                fileToUpload = file;
            }
        }
        
        String extension = getExtension(fileToUpload.getOriginalFilename());
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        
        // 上传到OSS（不再支持降级到本地存储）
        String fileUrl;
        String filePath;
        
        try {
            // 上传到OSS（使用压缩后的文件）
            fileUrl = ossService.uploadFile(fileToUpload, module);
            // 从URL中提取相对路径
            filePath = extractPathFromUrl(fileUrl);
            log.info("文件上传到OSS成功: {}", fileUrl);
        } catch (Exception e) {
            // OSS上传失败，直接抛出异常，不再降级到本地存储
            log.error("OSS上传失败，文件: {}, 模块: {}, 类型: {}, 错误: {}", 
                originalFilename, module, type, e.getMessage(), e);
            // 根据异常类型提供更友好的错误消息
            String errorMsg = buildErrorMessage(e);
            throw new IOException(errorMsg, e);
        }
        
        // 保存文件记录到数据库（保存原始URL，不保存签名URL，因为签名URL会过期）
        try {
            FileRecord record = new FileRecord();
            record.setFileName(fileName);
            record.setOriginalName(originalFilename);
            record.setFilePath(filePath);
            record.setFileUrl(fileUrl); // 保存原始URL
            record.setFileSize(fileToUpload.getSize());
            record.setFileType(type);
            record.setFileExtension(extension);
            record.setModule(module);
            record.setStorageType("OSS"); // 所有新上传的文件都使用OSS
            record.setCreatedAt(LocalDateTime.now());
            // TODO: 设置上传人ID（从SecurityContext获取）
            
            fileRecordService.saveFileRecord(record);
        } catch (Exception e) {
            log.error("保存文件记录失败", e);
            // 不影响文件上传结果
        }
        
        // 生成签名URL并返回（用于立即预览，避免403错误）
        // 注意：数据库保存的是原始URL，返回给前端的是签名URL
        try {
            String signedUrl = ossService.generateSignedUrlFromUrl(fileUrl);
            log.debug("生成签名URL成功，用于返回给前端");
            return signedUrl;
        } catch (Exception e) {
            log.warn("生成签名URL失败，返回原始URL: {}", e.getMessage());
            // 如果生成签名URL失败，返回原始URL（降级方案）
            return fileUrl;
        }
    }
    
    /**
     * 构建用户友好的错误消息
     * 
     * @param e 异常
     * @return 错误消息
     */
    private String buildErrorMessage(Exception e) {
        String errorMsg = e.getMessage();
        if (errorMsg == null || errorMsg.isEmpty()) {
            return "OSS上传失败，请检查OSS配置和网络连接后重试";
        }
        
        // 根据错误消息类型提供更友好的提示
        String lowerMsg = errorMsg.toLowerCase();
        if (lowerMsg.contains("config") || lowerMsg.contains("配置")) {
            return "OSS配置错误：" + errorMsg + "，请检查OSS配置";
        } else if (lowerMsg.contains("network") || lowerMsg.contains("网络") || lowerMsg.contains("connection")) {
            return "网络错误，OSS上传失败，请检查网络连接后重试";
        } else if (lowerMsg.contains("timeout") || lowerMsg.contains("超时")) {
            return "OSS上传超时，请稍后重试";
        } else if (lowerMsg.contains("unavailable") || lowerMsg.contains("不可用")) {
            return "OSS服务暂时不可用，请稍后重试或联系系统管理员";
        } else {
            return "OSS上传失败：" + errorMsg + "，请检查配置后重试";
        }
    }
    
    /**
     * 从OSS URL中提取相对路径
     * 
     * @param url OSS文件URL
     * @return 相对路径
     */
    private String extractPathFromUrl(String url) {
        // URL格式：https://{bucket}.{endpoint}/{path}
        int thirdSlashIndex = url.indexOf("/", url.indexOf("/", url.indexOf("/") + 1) + 1);
        if (thirdSlashIndex > 0) {
            return url.substring(thirdSlashIndex + 1);
        }
        return url;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * 验证图片扩展名
     */
    private boolean isValidImageExtension(String extension) {
        return "jpg".equals(extension) || "jpeg".equals(extension) 
            || "png".equals(extension) || "webp".equals(extension)
            || "gif".equals(extension) || "bmp".equals(extension);
    }
    
    /**
     * 验证视频扩展名
     */
    private boolean isValidVideoExtension(String extension) {
        return "mp4".equals(extension);
    }
    
    /**
     * 批量删除文件（同时删除OSS上的文件和数据库记录）
     * 
     * @param ids 文件ID列表
     * @return 删除结果（成功数量、失败数量、失败详情）
     */
    public Map<String, Object> deleteFilesBatch(List<Long> ids) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<Map<String, Object>> failDetails = new ArrayList<>();
        
        if (ids == null || ids.isEmpty()) {
            result.put("successCount", 0);
            result.put("failCount", 0);
            result.put("failDetails", failDetails);
            result.put("message", "文件ID列表为空");
            return result;
        }
        
        // 查询所有文件记录
        List<FileRecord> records = fileRecordService.getFileRecordsByIds(ids);
        if (records.isEmpty()) {
            result.put("successCount", 0);
            result.put("failCount", 0);
            result.put("failDetails", failDetails);
            result.put("message", "未找到任何文件记录");
            return result;
        }
        
        // 逐个删除文件
        for (FileRecord record : records) {
            try {
                boolean deleteSuccess = false;
                
                // 根据存储类型删除文件
                if ("OSS".equals(record.getStorageType())) {
                    // 删除OSS文件
                    try {
                        deleteSuccess = ossService.deleteFile(record.getFilePath());
                    } catch (Exception e) {
                        log.error("删除OSS文件失败：{}", record.getFilePath(), e);
                        Map<String, Object> failDetail = new HashMap<>();
                        failDetail.put("id", record.getId());
                        failDetail.put("fileName", record.getOriginalName());
                        failDetail.put("reason", "删除OSS文件失败：" + e.getMessage());
                        failDetails.add(failDetail);
                        failCount++;
                        continue;
                    }
                } else if ("LOCAL".equals(record.getStorageType())) {
                    // 删除本地文件
                    try {
                        String projectRoot = System.getProperty("user.dir");
                        java.io.File file = new java.io.File(projectRoot, "uploads/" + record.getFilePath());
                        if (file.exists()) {
                            deleteSuccess = file.delete();
                        } else {
                            log.warn("本地文件不存在：{}", file.getAbsolutePath());
                            deleteSuccess = true; // 文件不存在也算删除成功
                        }
                    } catch (Exception e) {
                        log.error("删除本地文件失败：{}", record.getFilePath(), e);
                        Map<String, Object> failDetail = new HashMap<>();
                        failDetail.put("id", record.getId());
                        failDetail.put("fileName", record.getOriginalName());
                        failDetail.put("reason", "删除本地文件失败：" + e.getMessage());
                        failDetails.add(failDetail);
                        failCount++;
                        continue;
                    }
                }
                
                // 删除数据库记录
                if (deleteSuccess) {
                    boolean dbDeleteSuccess = fileRecordService.deleteFileRecord(record.getId());
                    if (dbDeleteSuccess) {
                        successCount++;
                        log.info("删除文件成功，ID：{}，文件名：{}", record.getId(), record.getOriginalName());
                    } else {
                        Map<String, Object> failDetail = new HashMap<>();
                        failDetail.put("id", record.getId());
                        failDetail.put("fileName", record.getOriginalName());
                        failDetail.put("reason", "删除数据库记录失败");
                        failDetails.add(failDetail);
                        failCount++;
                    }
                } else {
                    Map<String, Object> failDetail = new java.util.HashMap<>();
                    failDetail.put("id", record.getId());
                    failDetail.put("fileName", record.getOriginalName());
                    failDetail.put("reason", "删除文件失败");
                    failDetails.add(failDetail);
                    failCount++;
                }
            } catch (Exception e) {
                log.error("删除文件异常，ID：{}", record.getId(), e);
                Map<String, Object> failDetail = new java.util.HashMap<>();
                failDetail.put("id", record.getId());
                failDetail.put("fileName", record.getOriginalName());
                failDetail.put("reason", "删除异常：" + e.getMessage());
                failDetails.add(failDetail);
                failCount++;
            }
        }
        
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failDetails", failDetails);
        result.put("total", ids.size());
        result.put("message", String.format("删除完成：成功 %d 个，失败 %d 个", successCount, failCount));
        
        return result;
    }
}
