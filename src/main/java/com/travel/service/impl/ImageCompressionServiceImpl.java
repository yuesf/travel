package com.travel.service.impl;

import com.travel.service.ImageCompressionService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

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
    
    @Value("${travel.file.webp.force-compress:false}")
    private boolean forceCompress;
    
    @Value("${travel.file.webp.quality:80}")
    private int webpQuality;
    
    @Value("${travel.file.webp.max-width:1080}")
    private int maxWidth;
    
    @Value("${travel.file.webp.min-file-size:51200}")
    private long minFileSize;
    
    @Override
    public MultipartFile compressToWebP(MultipartFile file) throws IOException {
        if (!webpEnabled) {
            log.error("WebP 压缩已禁用，但系统要求必须使用 WebP 压缩，请检查配置");
            throw new IOException("WebP 压缩已禁用，无法上传图片。请启用 WebP 压缩功能（travel.file.webp.enabled=true）");
        }
        
        // 不再跳过小文件，所有图片都必须压缩
        // if (shouldSkipCompression(file)) {
        //     log.debug("文件过小，跳过压缩: {} bytes", file.getSize());
        //     return file;
        // }
        
        try {
            // 读取原始图片
            InputStream originalInputStream = file.getInputStream();
            BufferedImage originalImage = ImageIO.read(originalInputStream);
            
            if (originalImage == null) {
                throw new IOException("无法读取图片文件，可能不是有效的图片格式");
            }
            
            // 判断是否需要压缩
            boolean needCompress = false;
            String skipReason = null;
            
            if (forceCompress) {
                // 强制压缩模式：所有图片都压缩
                needCompress = true;
                log.debug("强制压缩模式：图片将被压缩");
            } else {
                // 阈值模式：根据大小和分辨率判断
                int imageWidth = originalImage.getWidth();
                long fileSize = file.getSize();
                
                boolean exceedsWidth = maxWidth > 0 && imageWidth > maxWidth;
                boolean exceedsSize = minFileSize > 0 && fileSize > minFileSize;
                
                if (exceedsWidth || exceedsSize) {
                    needCompress = true;
                    log.debug("图片超过阈值，需要压缩: 宽度={}px (阈值={}px), 大小={} bytes (阈值={} bytes)", 
                            imageWidth, maxWidth, fileSize, minFileSize);
                } else {
                    skipReason = String.format("图片未超过阈值: 宽度=%dpx (阈值=%dpx), 大小=%d bytes (阈值=%d bytes)", 
                            imageWidth, maxWidth, fileSize, minFileSize);
                    log.debug(skipReason);
                }
            }
            
            // 如果不需要压缩，返回原始文件
            if (!needCompress) {
                log.debug("跳过 WebP 压缩，使用原始文件。原因: {}", skipReason != null ? skipReason : "强制压缩已禁用");
                return file;
            }
            
            // 如果设置了最大宽度且图片宽度大于最大宽度，先进行缩放
            BufferedImage processedImage = originalImage;
            int imageWidth = originalImage.getWidth();
            if (maxWidth > 0 && imageWidth > maxWidth) {
                int newHeight = (int) ((double) originalImage.getHeight() * maxWidth / originalImage.getWidth());
                processedImage = Thumbnails.of(originalImage)
                        .width(maxWidth)
                        .height(newHeight)
                        .asBufferedImage();
                log.debug("图片已缩放: {}x{} -> {}x{}", 
                        originalImage.getWidth(), originalImage.getHeight(),
                        processedImage.getWidth(), processedImage.getHeight());
            }
            
            // 压缩为 WebP 格式
            byte[] compressedBytes = convertToWebP(processedImage, webpQuality);
            
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
            log.error("图片压缩失败: {}", e.getMessage(), e);
            throw new IOException("图片压缩失败，无法上传图片: " + e.getMessage(), e);
        }
    }
    
    @Override
    public InputStream compressToWebP(InputStream inputStream, String originalFilename) throws IOException {
        if (!webpEnabled) {
            log.error("WebP 压缩已禁用，但系统要求必须使用 WebP 压缩，请检查配置");
            throw new IOException("WebP 压缩已禁用，无法上传图片。请启用 WebP 压缩功能（travel.file.webp.enabled=true）");
        }
        
        try {
            // 读取原始图片
            BufferedImage originalImage = ImageIO.read(inputStream);
            
            if (originalImage == null) {
                throw new IOException("无法读取图片文件，可能不是有效的图片格式");
            }
            
            // 判断是否需要压缩
            boolean needCompress = false;
            String skipReason = null;
            
            if (forceCompress) {
                // 强制压缩模式：所有图片都压缩
                needCompress = true;
                log.debug("强制压缩模式：图片将被压缩");
            } else {
                // 阈值模式：根据分辨率判断（InputStream 方法无法获取文件大小，只根据分辨率）
                int imageWidth = originalImage.getWidth();
                
                boolean exceedsWidth = maxWidth > 0 && imageWidth > maxWidth;
                
                if (exceedsWidth) {
                    needCompress = true;
                    log.debug("图片宽度超过阈值，需要压缩: 宽度={}px (阈值={}px)", imageWidth, maxWidth);
                } else {
                    skipReason = String.format("图片宽度未超过阈值: 宽度=%dpx (阈值=%dpx)", imageWidth, maxWidth);
                    log.debug(skipReason);
                }
            }
            
            // 如果不需要压缩，由于输入流已读取，需要将图片转换回字节流
            if (!needCompress) {
                log.debug("跳过 WebP 压缩。原因: {}", skipReason != null ? skipReason : "强制压缩已禁用");
                // 将 BufferedImage 转换回字节流（保持原始格式）
                // 注意：这里无法确定原始格式，所以使用 PNG 作为默认格式
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(originalImage, "png", outputStream);
                return new ByteArrayInputStream(outputStream.toByteArray());
            }
            
            // 如果设置了最大宽度且图片宽度大于最大宽度，先进行缩放
            BufferedImage processedImage = originalImage;
            int imageWidth = originalImage.getWidth();
            if (maxWidth > 0 && imageWidth > maxWidth) {
                int newHeight = (int) ((double) originalImage.getHeight() * maxWidth / originalImage.getWidth());
                processedImage = Thumbnails.of(originalImage)
                        .width(maxWidth)
                        .height(newHeight)
                        .asBufferedImage();
                log.debug("图片已缩放: {}x{} -> {}x{}", 
                        originalImage.getWidth(), originalImage.getHeight(),
                        processedImage.getWidth(), processedImage.getHeight());
            }
            
            // 压缩为 WebP 格式
            byte[] compressedBytes = convertToWebP(processedImage, webpQuality);
            log.info("图片压缩成功: 压缩后大小 {} bytes", compressedBytes.length);
            
            return new ByteArrayInputStream(compressedBytes);
            
        } catch (Exception e) {
            log.error("图片压缩失败: {}", e.getMessage(), e);
            throw new IOException("图片压缩失败，无法上传图片: " + e.getMessage(), e);
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
     * 将 BufferedImage 转换为 WebP 格式的字节数组
     * 
     * @param image 图片对象
     * @param quality 压缩质量 (0-100)
     * @return WebP 格式的字节数组
     * @throws IOException 转换失败时抛出异常
     */
    private byte[] convertToWebP(BufferedImage image, int quality) throws IOException {
        // 获取 WebP 图片写入器
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (!writers.hasNext()) {
            throw new IOException("系统不支持 WebP 格式，请检查是否已添加 webp-imageio 依赖");
        }
        
        ImageWriter writer = writers.next();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try (ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(imageOutputStream);
            
            // 获取写入参数
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            
            // 检查是否支持压缩
            if (writeParam.canWriteCompressed()) {
                // 设置压缩模式为显式模式
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                
                // 获取可用的压缩类型
                String[] compressionTypes = writeParam.getCompressionTypes();
                if (compressionTypes != null && compressionTypes.length > 0) {
                    // 优先使用有损压缩（Lossy），如果没有则使用第一个可用的
                    String compressionType = null;
                    for (String type : compressionTypes) {
                        if (type != null && type.toLowerCase().contains("lossy")) {
                            compressionType = type;
                            break;
                        }
                    }
                    // 如果没找到有损压缩，使用第一个
                    if (compressionType == null) {
                        compressionType = compressionTypes[0];
                    }
                    try {
                        writeParam.setCompressionType(compressionType);
                        log.debug("设置压缩类型: {}", compressionType);
                    } catch (Exception e) {
                        log.warn("设置压缩类型失败: {}, 尝试其他方式", e.getMessage());
                        // 如果设置失败，尝试直接使用第一个类型
                        if (compressionTypes.length > 0) {
                            try {
                                writeParam.setCompressionType(compressionTypes[0]);
                            } catch (Exception e2) {
                                throw new IOException("无法设置 WebP 压缩类型: " + e2.getMessage(), e2);
                            }
                        }
                    }
                } else {
                    // 如果没有压缩类型，尝试使用反射或直接设置
                    try {
                        // 尝试使用反射访问 WebPWriteParam（如果存在）
                        Class<?> paramClass = writeParam.getClass();
                        if (paramClass.getName().contains("WebP")) {
                            // 尝试设置常见的压缩类型
                            String[] commonTypes = {"Lossy", "LOSSLESS", "lossy", "lossless", "LOSSY"};
                            boolean set = false;
                            for (String type : commonTypes) {
                                try {
                                    writeParam.setCompressionType(type);
                                    log.debug("使用压缩类型: {}", type);
                                    set = true;
                                    break;
                                } catch (Exception e) {
                                    // 继续尝试下一个
                                }
                            }
                            if (!set) {
                                throw new IOException("无法设置 WebP 压缩类型，请检查 webp-imageio 库版本");
                            }
                        } else {
                            throw new IOException("WebP 写入参数类型不正确: " + paramClass.getName());
                        }
                    } catch (Exception e) {
                        throw new IOException("无法设置 WebP 压缩类型: " + e.getMessage(), e);
                    }
                }
                
                // 设置压缩质量 (0.0-1.0)
                float qualityFloat = Math.max(0.0f, Math.min(1.0f, quality / 100.0f));
                writeParam.setCompressionQuality(qualityFloat);
                log.debug("WebP 压缩参数: 压缩类型={}, 质量={}", 
                        writeParam.getCompressionType(), qualityFloat);
            } else {
                log.warn("WebP 写入器不支持压缩，使用默认设置");
            }
            
            // 写入图片
            writer.write(null, new IIOImage(image, null, null), writeParam);
        } finally {
            writer.dispose();
        }
        
        return outputStream.toByteArray();
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
