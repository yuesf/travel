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
}
