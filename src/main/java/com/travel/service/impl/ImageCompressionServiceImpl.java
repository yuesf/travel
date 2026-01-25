package com.travel.service.impl;

import com.travel.service.ImageCompressionService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片压缩服务实现类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class ImageCompressionServiceImpl implements ImageCompressionService {
    
    @Value("${travel.file.webp.enabled:true}")
    private boolean webpEnabled;
    
    @Value("${travel.file.webp.quality:80}")
    private int webpQuality;
    
    @Value("${travel.file.webp.max-width:1920}")
    private int maxWidth;
    
    @Value("${travel.file.webp.min-file-size:51200}")
    private long minFileSize;
    
    @Override
    public MultipartFile compressToWebP(MultipartFile file) {
        if (!webpEnabled) {
            log.debug("WebP 压缩已禁用，跳过压缩");
            return file;
        }
        
        if (shouldSkipCompression(file)) {
            log.debug("文件过小，跳过压缩: {} bytes", file.getSize());
            return file;
        }
        
        try {
            // 读取原始图片
            InputStream originalInputStream = file.getInputStream();
            
            // 压缩为 WebP 格式
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(originalInputStream)
                    .outputFormat("webp")
                    .outputQuality(webpQuality / 100.0);
            
            // 如果设置了最大宽度，则进行缩放
            if (maxWidth > 0) {
                builder.width(maxWidth);
            }
            
            builder.toOutputStream(outputStream);
            
            byte[] compressedBytes = outputStream.toByteArray();
            log.info("图片压缩成功: 原始大小 {} bytes, 压缩后大小 {} bytes, 压缩率 {:.2f}%",
                    file.getSize(), compressedBytes.length, 
                    (1.0 - (double) compressedBytes.length / file.getSize()) * 100);
            
            // 创建压缩后的 MultipartFile
            return new CompressedMultipartFile(
                    file,
                    compressedBytes,
                    getWebpFilename(file.getOriginalFilename())
            );
            
        } catch (Exception e) {
            log.warn("图片压缩失败，使用原始文件: {}", e.getMessage());
            return file; // 压缩失败时返回原始文件
        }
    }
    
    @Override
    public InputStream compressToWebP(InputStream inputStream, String originalFilename) {
        if (!webpEnabled) {
            log.debug("WebP 压缩已禁用，跳过压缩");
            return inputStream;
        }
        
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(inputStream)
                    .outputFormat("webp")
                    .outputQuality(webpQuality / 100.0);
            
            if (maxWidth > 0) {
                builder.width(maxWidth);
            }
            
            builder.toOutputStream(outputStream);
            
            byte[] compressedBytes = outputStream.toByteArray();
            log.info("图片压缩成功: 压缩后大小 {} bytes", compressedBytes.length);
            
            return new ByteArrayInputStream(compressedBytes);
            
        } catch (Exception e) {
            log.warn("图片压缩失败，使用原始输入流: {}", e.getMessage());
            return inputStream; // 压缩失败时返回原始输入流
        }
    }
    
    @Override
    public boolean shouldSkipCompression(MultipartFile file) {
        if (minFileSize <= 0) {
            return false; // 不跳过
        }
        
        return file.getSize() < minFileSize;
    }
    
    /**
     * 获取 WebP 格式的文件名
     */
    private String getWebpFilename(String originalFilename) {
        if (originalFilename == null) {
            return "image.webp";
        }
        
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return originalFilename.substring(0, lastDotIndex) + ".webp";
        }
        
        return originalFilename + ".webp";
    }
    
    /**
     * 压缩后的 MultipartFile 包装类
     */
    private static class CompressedMultipartFile implements MultipartFile {
        private final MultipartFile originalFile;
        private final byte[] compressedBytes;
        private final String filename;
        
        public CompressedMultipartFile(MultipartFile originalFile, byte[] compressedBytes, String filename) {
            this.originalFile = originalFile;
            this.compressedBytes = compressedBytes;
            this.filename = filename;
        }
        
        @Override
        @NonNull
        public String getName() {
            String name = originalFile.getName();
            return name != null ? name : "";
        }
        
        @Override
        public String getOriginalFilename() {
            return filename != null ? filename : "image.webp";
        }
        
        @Override
        public String getContentType() {
            return "image/webp";
        }
        
        @Override
        public boolean isEmpty() {
            return compressedBytes == null || compressedBytes.length == 0;
        }
        
        @Override
        public long getSize() {
            return compressedBytes != null ? compressedBytes.length : 0;
        }
        
        @Override
        @NonNull
        public byte[] getBytes() throws IOException {
            if (compressedBytes == null) {
                return new byte[0];
            }
            return compressedBytes;
        }
        
        @Override
        @NonNull
        public InputStream getInputStream() throws IOException {
            if (compressedBytes == null) {
                return new ByteArrayInputStream(new byte[0]);
            }
            return new ByteArrayInputStream(compressedBytes);
        }
        
        @Override
        public void transferTo(@NonNull java.io.File dest) throws IOException, IllegalStateException {
            if (compressedBytes == null) {
                throw new IOException("压缩后的字节数组为空");
            }
            java.nio.file.Files.write(dest.toPath(), compressedBytes);
        }
    }
}
