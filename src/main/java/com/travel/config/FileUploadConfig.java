package com.travel.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件上传配置类
 * 
 * @author travel-platform
 */
@Slf4j
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {
    
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
     * 初始化上传目录
     */
    @PostConstruct
    public void initUploadDirectory() {
        try {
            Path uploadDir = getActualUploadPath();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("创建文件上传目录: {}", uploadDir.toAbsolutePath());
            } else {
                log.info("文件上传目录已存在: {}", uploadDir.toAbsolutePath());
            }
        } catch (Exception e) {
            log.error("初始化文件上传目录失败", e);
            throw new RuntimeException("初始化文件上传目录失败", e);
        }
    }
    
    /**
     * 配置静态资源访问
     * 将 /uploads/** 映射到本地文件系统的上传目录
     * 注意：前端静态资源已分离到 nginx，不再由后端提供
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 从 access-url 中提取路径前缀（例如：http://localhost:8080/uploads -> /uploads）
        // 或者：https://yuesf.cn/travel/uploads -> /travel/uploads
        String pathPattern = extractPathFromUrl(accessUrl);
        
        // 获取实际上传路径（如果是相对路径，则基于项目根目录）
        Path actualUploadPath = getActualUploadPath();
        
        // 添加上传文件的静态资源处理器（使用提取的路径）
        registry.addResourceHandler(pathPattern + "/**")
                .addResourceLocations("file:" + actualUploadPath.toAbsolutePath() + "/");
        
        // 同时注册 /uploads/** 路径，以兼容 Nginx 代理去掉 /travel/ 前缀的情况
        // 这样无论 Nginx 是否保留前缀，都能正常访问
        if (!pathPattern.equals("/uploads")) {
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:" + actualUploadPath.toAbsolutePath() + "/");
            log.info("同时注册兼容路径: /uploads/** -> {}", actualUploadPath.toAbsolutePath());
        }
        
        log.info("配置静态资源访问: {} -> {}", pathPattern + "/**", actualUploadPath.toAbsolutePath());
        log.info("前端静态资源已分离到 nginx，不再由后端提供");
        
        // 注意：
        // 1. 前端静态资源已分离，由 nginx 直接提供（/admin/ 路径）
        // 2. 后端处理上传文件的静态资源访问，同时支持 /uploads/** 和 /travel/uploads/**
        // 3. /static/** 路径由 StaticResourceController 处理，返回 404
    }
    
    /**
     * 从URL中提取路径部分
     * 例如：http://localhost:8080/uploads -> /uploads
     */
    private String extractPathFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "/uploads";
        }
        
        // 如果URL包含协议，提取路径部分
        if (url.startsWith("http://") || url.startsWith("https://")) {
            int pathStartIndex = url.indexOf('/', url.indexOf("://") + 3);
            if (pathStartIndex != -1) {
                return url.substring(pathStartIndex);
            }
            return "/uploads";
        }
        
        // 如果已经是路径格式，直接返回
        if (url.startsWith("/")) {
            return url;
        }
        
        // 默认返回 /uploads
        return "/uploads";
    }
}
