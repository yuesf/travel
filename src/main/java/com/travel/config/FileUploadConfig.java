package com.travel.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置类
 * 
 * 注意：已移除本地存储上传功能，不再需要初始化上传目录和静态资源映射。
 * 所有新文件上传到OSS，历史本地文件的访问由Nginx或其他静态资源服务器处理。
 * 
 * @author travel-platform
 */
@Slf4j
@Configuration
public class FileUploadConfig {
    // 已移除所有本地存储相关配置
    // 不再需要初始化上传目录和静态资源映射
    // 所有文件上传功能由 FileService 通过 OSS 处理
}
