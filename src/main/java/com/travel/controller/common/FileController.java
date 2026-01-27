package com.travel.controller.common;

import com.travel.common.Result;
import com.travel.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/common/file")
public class FileController {
    
    @Autowired
    private FileService fileService;
    
    /**
     * 上传图片
     * 
     * @param file 图片文件
     * @param module 模块名称（可选，默认为common）
     * @param directoryId 目录ID（可选，如果提供则使用目录路径作为module）
     * @return 上传结果
     */
    @PostMapping("/upload/image")
    public Result<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "module", required = false, defaultValue = "common") String module,
            @RequestParam(value = "directoryId", required = false) Long directoryId) {
        try {
            String url = fileService.uploadImage(file, module, directoryId);
            return Result.success(url);
        } catch (IllegalArgumentException e) {
            log.warn("图片上传失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("图片上传异常", e);
            return Result.error("图片上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 上传视频
     * 
     * @param file 视频文件
     * @param module 模块名称（可选，默认为common）
     * @param directoryId 目录ID（可选，如果提供则使用目录路径作为module）
     * @return 上传结果
     */
    @PostMapping("/upload/video")
    public Result<String> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "module", required = false, defaultValue = "common") String module,
            @RequestParam(value = "directoryId", required = false) Long directoryId) {
        try {
            String url = fileService.uploadVideo(file, module, directoryId);
            return Result.success(url);
        } catch (IllegalArgumentException e) {
            log.warn("视频上传失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("视频上传异常", e);
            return Result.error("视频上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据URL获取公开URL（已废弃：OSS bucket已改为"私有写公有读"模式，不再需要签名URL）
     * 保留此接口用于向后兼容，如果是签名URL则提取基础URL部分，否则直接返回原URL
     * 
     * @param url 文件访问URL（可能是签名URL或公开URL）
     * @return 公开URL，如果不是OSS URL则返回原URL
     */
    @GetMapping("/signed-url")
    public Result<String> getSignedUrl(@RequestParam String url) {
        try {
            // OSS bucket已改为"私有写公有读"模式，直接返回公开URL
            // 如果是签名URL，提取基础URL部分（兼容历史数据）
            int queryIndex = url.indexOf('?');
            if (queryIndex > 0) {
                String queryString = url.substring(queryIndex + 1);
                if (queryString.contains("Expires=") || queryString.contains("Signature=")) {
                    // 从签名URL提取公开URL
                    String publicUrl = url.substring(0, queryIndex);
                    return Result.success(publicUrl);
                }
            }
            
            // 已经是公开URL，直接返回
            return Result.success(url);
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", url, e);
            // 如果处理失败，返回原URL
            return Result.success(url);
        }
    }
}
