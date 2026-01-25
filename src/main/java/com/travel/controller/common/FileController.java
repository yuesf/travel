package com.travel.controller.common;

import com.travel.common.Result;
import com.travel.service.FileService;
import com.travel.service.OssService;
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
    
    @Autowired
    private OssService ossService;
    
    /**
     * 上传图片
     * 
     * @param file 图片文件
     * @param module 模块名称（可选，默认为common）
     * @return 上传结果
     */
    @PostMapping("/upload/image")
    public Result<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "module", required = false, defaultValue = "common") String module) {
        try {
            String url = fileService.uploadImage(file, module);
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
     * @return 上传结果
     */
    @PostMapping("/upload/video")
    public Result<String> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "module", required = false, defaultValue = "common") String module) {
        try {
            String url = fileService.uploadVideo(file, module);
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
     * 根据URL获取签名URL（用于私有Bucket访问）
     * 
     * @param url 文件访问URL
     * @return 签名URL，如果不是OSS URL则返回原URL
     */
    @GetMapping("/signed-url")
    public Result<String> getSignedUrl(@RequestParam String url) {
        try {
            String signedUrl = ossService.generateSignedUrlFromUrl(url);
            return Result.success(signedUrl);
        } catch (Exception e) {
            log.error("获取签名URL失败: {}", url, e);
            // 如果获取失败，返回原URL
            return Result.success(url);
        }
    }
}
