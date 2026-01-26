package com.travel.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 图片压缩服务接口
 * 
 * @author travel-platform
 */
public interface ImageCompressionService {
    
    /**
     * 将图片压缩为 WebP 格式
     * 
     * @param file 原始图片文件
     * @return 压缩后的 MultipartFile（WebP 格式）
     * @throws IOException 压缩失败时抛出异常，不允许使用原始文件
     */
    MultipartFile compressToWebP(MultipartFile file) throws IOException;
    
    /**
     * 将图片输入流压缩为 WebP 格式
     * 
     * @param inputStream 原始图片输入流
     * @param originalFilename 原始文件名
     * @return 压缩后的输入流（WebP 格式）
     * @throws IOException 压缩失败时抛出异常，不允许使用原始输入流
     */
    InputStream compressToWebP(InputStream inputStream, String originalFilename) throws IOException;
    
    /**
     * 检查是否应该跳过压缩
     * 
     * @param file 图片文件
     * @return true-跳过压缩，false-进行压缩
     */
    boolean shouldSkipCompression(MultipartFile file);
}
