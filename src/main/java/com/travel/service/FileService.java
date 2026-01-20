package com.travel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传服务
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class FileService {
    
    @Value("${travel.file.upload-path}")
    private String uploadPath;
    
    @Value("${travel.file.access-url}")
    private String accessUrl;
    
    /**
     * 获取实际上传路径（如果是相对路径，则基于项目根目录）
     */
    private Path getActualUploadPath() {
        Path path = Paths.get(uploadPath);
        // 如果是相对路径，则基于项目根目录（user.dir）
        if (!path.isAbsolute()) {
            String projectRoot = System.getProperty("user.dir");
            path = Paths.get(projectRoot, uploadPath);
            log.debug("使用相对路径，项目根目录: {}, 实际上传路径: {}", projectRoot, path.toAbsolutePath());
        }
        return path;
    }
    
    /**
     * 上传图片
     * 
     * @param file 图片文件
     * @param module 模块名称（如：banner, attraction等）
     * @return 文件访问URL
     * @throws IOException IO异常
     */
    public String uploadImage(MultipartFile file, String module) throws IOException {
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只能上传图片文件");
        }
        
        // 验证文件大小（5MB）
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("图片大小不能超过5MB");
        }
        
        // 验证文件格式
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        String extension = getExtension(originalFilename);
        if (!isValidImageExtension(extension)) {
            throw new IllegalArgumentException("只支持JPG、PNG、WebP格式的图片");
        }
        
        return uploadFile(file, module, "image");
    }
    
    /**
     * 上传视频
     * 
     * @param file 视频文件
     * @param module 模块名称（如：banner, attraction等）
     * @return 文件访问URL
     * @throws IOException IO异常
     */
    public String uploadVideo(MultipartFile file, String module) throws IOException {
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("只能上传视频文件");
        }
        
        // 验证文件大小（50MB）
        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("视频大小不能超过50MB");
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
     * 上传文件通用方法
     * 
     * @param file 文件
     * @param module 模块名称
     * @param type 文件类型（image或video）
     * @return 文件访问URL
     * @throws IOException IO异常
     */
    private String uploadFile(MultipartFile file, String module, String type) throws IOException {
        // 生成文件路径：{模块}/{类型}/{日期}/{随机UUID}.{扩展名}
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String extension = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        
        // 构建目录路径
        String relativePath = String.format("%s/%s/%s/%s", module, type, dateStr, fileName);
        
        // 获取实际上传路径（如果是相对路径，则基于项目根目录）
        Path baseUploadPath = getActualUploadPath();
        Path uploadDir = baseUploadPath.resolve(module).resolve(type).resolve(dateStr);
        
        // 创建目录（如果不存在）
        Files.createDirectories(uploadDir);
        
        // 保存文件
        Path filePath = uploadDir.resolve(fileName);
        file.transferTo(filePath.toFile());
        
        log.info("文件上传成功: {}", filePath.toAbsolutePath());
        
        // 返回访问URL
        return accessUrl + "/" + relativePath;
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
            || "png".equals(extension) || "webp".equals(extension);
    }
    
    /**
     * 验证视频扩展名
     */
    private boolean isValidVideoExtension(String extension) {
        return "mp4".equals(extension);
    }
}
