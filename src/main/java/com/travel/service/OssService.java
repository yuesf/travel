package com.travel.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * OSS文件服务接口
 * 
 * @author travel-platform
 */
public interface OssService {
    
    /**
     * 检查OSS是否已配置且启用
     * @return true-已配置且启用，false-未配置或已禁用
     */
    boolean isOssEnabled();
    
    /**
     * 上传文件到OSS
     * @param file 上传的文件
     * @param module 模块名称（如：common、article、banner等）
     * @return 文件访问URL
     * @throws Exception 上传失败时抛出异常
     */
    String uploadFile(MultipartFile file, String module) throws Exception;
    
    /**
     * 上传文件到OSS（使用输入流）
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @param module 模块名称
     * @return 文件访问URL
     * @throws Exception 上传失败时抛出异常
     */
    String uploadFile(InputStream inputStream, String fileName, String module) throws Exception;
    
    /**
     * 从OSS删除文件
     * @param filePath 文件路径（相对路径）
     * @return true-删除成功，false-删除失败
     * @throws Exception 删除失败时抛出异常
     */
    boolean deleteFile(String filePath) throws Exception;
    
    /**
     * 检查文件是否存在于OSS
     * @param filePath 文件路径（相对路径）
     * @return true-文件存在，false-文件不存在
     */
    boolean fileExists(String filePath);
    
    /**
     * 获取文件的访问URL
     * @param filePath 文件路径（相对路径）
     * @return 文件访问URL
     */
    String getFileUrl(String filePath);
    
    /**
     * 生成签名URL（用于私有Bucket访问）
     * @param filePath 文件路径（相对路径）
     * @param expireSeconds 过期时间（秒），默认3600秒（1小时）
     * @return 签名后的URL
     */
    String generateSignedUrl(String filePath, int expireSeconds);
    
    /**
     * 生成签名URL（默认1小时有效期）
     * @param filePath 文件路径（相对路径）
     * @return 签名后的URL
     */
    default String generateSignedUrl(String filePath) {
        return generateSignedUrl(filePath, 3600);
    }
    
    /**
     * 根据URL生成签名URL（用于私有Bucket访问）
     * @param url 文件访问URL
     * @param expireSeconds 过期时间（秒），默认3600秒（1小时）
     * @return 签名后的URL，如果不是OSS URL则返回原URL
     */
    String generateSignedUrlFromUrl(String url, int expireSeconds);
    
    /**
     * 根据URL生成签名URL（默认1小时有效期）
     * @param url 文件访问URL
     * @return 签名后的URL，如果不是OSS URL则返回原URL
     */
    default String generateSignedUrlFromUrl(String url) {
        return generateSignedUrlFromUrl(url, 3600);
    }
}
