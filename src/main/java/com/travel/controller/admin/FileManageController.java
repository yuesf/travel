package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.FileRecordQueryRequest;
import com.travel.dto.FileRecordResponse;
import com.travel.dto.FileStatisticsResponse;
import com.travel.entity.FileRecord;
import com.travel.service.FileRecordService;
import com.travel.service.FileService;
import com.travel.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/file-manage")
public class FileManageController {
    
    @Autowired
    private FileRecordService fileRecordService;
    
    @Autowired
    private OssService ossService;
    
    @Autowired
    private FileService fileService;
    
    /**
     * 查询文件列表（分页）
     * 
     * @param request 查询请求
     * @return 文件列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> queryFileRecords(FileRecordQueryRequest request) {
        try {
            Map<String, Object> result = fileRecordService.queryFileRecords(
                request.getFileType(),
                request.getModule(),
                request.getStorageType(),
                request.getKeyword(),
                request.getStartDate(),
                request.getEndDate(),
                request.getPage(),
                request.getPageSize()
            );
            
            // 转换为DTO
            @SuppressWarnings("unchecked")
            List<FileRecord> records = (List<FileRecord>) result.get("records");
            List<FileRecordResponse> responses = new ArrayList<>();
            
            for (FileRecord record : records) {
                FileRecordResponse response = new FileRecordResponse();
                BeanUtils.copyProperties(record, response);
                response.setFileSizeFormatted(formatFileSize(record.getFileSize()));
                
                // OSS bucket已改为"私有写公有读"模式，直接使用公开URL
                // 如果是OSS文件，直接使用fileUrl作为previewUrl
                if ("OSS".equals(record.getStorageType()) && ossService.isOssEnabled()) {
                    // 直接使用fileUrl（公开URL），无需生成签名URL
                    response.setPreviewUrl(record.getFileUrl());
                }
                
                responses.add(response);
            }
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("records", responses);
            responseMap.put("total", result.get("total"));
            responseMap.put("page", result.get("page"));
            responseMap.put("pageSize", result.get("pageSize"));
            responseMap.put("totalPages", result.get("totalPages"));
            
            return Result.success(responseMap);
        } catch (Exception e) {
            log.error("查询文件列表失败", e);
            return Result.error("查询文件列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询文件详情
     * 
     * @param id 文件ID
     * @return 文件详情
     */
    @GetMapping("/{id}")
    public Result<FileRecordResponse> getFileRecordById(@PathVariable Long id) {
        try {
            FileRecord record = fileRecordService.getFileRecordById(id);
            if (record == null) {
                return Result.error("文件不存在");
            }
            
            FileRecordResponse response = new FileRecordResponse();
            BeanUtils.copyProperties(record, response);
            response.setFileSizeFormatted(formatFileSize(record.getFileSize()));
            
            // OSS bucket已改为"私有写公有读"模式，直接使用公开URL
            // 如果是OSS文件，直接使用fileUrl作为previewUrl
            if ("OSS".equals(record.getStorageType()) && ossService.isOssEnabled()) {
                // 直接使用fileUrl（公开URL），无需生成签名URL
                response.setPreviewUrl(record.getFileUrl());
            }
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("查询文件详情失败", e);
            return Result.error("查询文件详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量删除文件
     * 
     * @param ids 文件ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    public Result<Map<String, Object>> deleteFilesBatch(@RequestBody List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("文件ID列表不能为空");
            }
            
            // 使用FileService的批量删除方法
            Map<String, Object> result = fileService.deleteFilesBatch(ids);
            
            int successCount = (Integer) result.get("successCount");
            int failCount = (Integer) result.get("failCount");
            
            if (failCount == 0) {
                return Result.success("批量删除成功，共删除 " + successCount + " 个文件", result);
            } else if (successCount == 0) {
                return Result.error("批量删除失败：" + result.get("message"));
            } else {
                return Result.success("部分删除成功：成功 " + successCount + " 个，失败 " + failCount + " 个", result);
            }
        } catch (Exception e) {
            log.error("批量删除文件异常", e);
            return Result.error("批量删除文件失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除文件
     * 
     * @param id 文件ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteFile(@PathVariable Long id) {
        try {
            // 查询文件记录
            FileRecord record = fileRecordService.getFileRecordById(id);
            if (record == null) {
                return Result.error("文件不存在");
            }
            
            // 根据存储类型删除文件
            boolean deleteSuccess = false;
            if ("OSS".equals(record.getStorageType())) {
                // 删除OSS文件
                try {
                    deleteSuccess = ossService.deleteFile(record.getFilePath());
                } catch (Exception e) {
                    log.error("删除OSS文件失败：{}", record.getFilePath(), e);
                    return Result.error("删除OSS文件失败：" + e.getMessage());
                }
            } else if ("LOCAL".equals(record.getStorageType())) {
                // 删除本地文件
                try {
                    String projectRoot = System.getProperty("user.dir");
                    File file = new File(projectRoot, "uploads/" + record.getFilePath());
                    if (file.exists()) {
                        deleteSuccess = file.delete();
                    } else {
                        log.warn("本地文件不存在：{}", file.getAbsolutePath());
                        deleteSuccess = true; // 文件不存在也算删除成功
                    }
                } catch (Exception e) {
                    log.error("删除本地文件失败：{}", record.getFilePath(), e);
                    return Result.error("删除本地文件失败：" + e.getMessage());
                }
            }
            
            // 删除数据库记录
            if (deleteSuccess) {
                boolean dbDeleteSuccess = fileRecordService.deleteFileRecord(id);
                if (dbDeleteSuccess) {
                    return Result.success("删除文件成功");
                } else {
                    return Result.error("删除文件记录失败");
                }
            } else {
                return Result.error("删除文件失败");
            }
        } catch (Exception e) {
            log.error("删除文件异常", e);
            return Result.error("删除文件失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取文件统计信息
     * 
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public Result<FileStatisticsResponse> getFileStatistics() {
        try {
            Map<String, Object> stats = fileRecordService.getFileStatistics();
            
            FileStatisticsResponse response = new FileStatisticsResponse();
            response.setTotalCount((Integer) stats.get("totalCount"));
            response.setTotalSize((Long) stats.get("totalSize"));
            response.setOssCount((Integer) stats.get("ossCount"));
            response.setOssSize((Long) stats.get("ossSize"));
            response.setLocalCount((Integer) stats.get("localCount"));
            response.setLocalSize((Long) stats.get("localSize"));
            
            // 格式化文件大小
            response.setTotalSizeFormatted(formatFileSize(response.getTotalSize()));
            response.setOssSizeFormatted(formatFileSize(response.getOssSize()));
            response.setLocalSizeFormatted(formatFileSize(response.getLocalSize()));
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取文件统计信息失败", e);
            return Result.error("获取文件统计信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取文件的公开URL（已废弃：OSS bucket已改为"私有写公有读"模式，不再需要签名URL）
     * 保留此接口用于向后兼容，直接返回公开URL
     * 
     * @param id 文件ID
     * @return 公开URL
     */
    @GetMapping("/{id}/signed-url")
    public Result<String> getSignedUrl(@PathVariable Long id) {
        try {
            // 查询文件记录
            FileRecord record = fileRecordService.getFileRecordById(id);
            if (record == null) {
                return Result.error("文件不存在");
            }
            
            // OSS bucket已改为"私有写公有读"模式，直接返回公开URL
            // 如果是签名URL，提取基础URL部分（兼容历史数据）
            String fileUrl = record.getFileUrl();
            int queryIndex = fileUrl.indexOf('?');
            if (queryIndex > 0) {
                String queryString = fileUrl.substring(queryIndex + 1);
                if (queryString.contains("Expires=") || queryString.contains("Signature=")) {
                    // 从签名URL提取公开URL
                    fileUrl = fileUrl.substring(0, queryIndex);
                }
            }
            
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("获取文件URL失败", e);
            return Result.error("获取文件URL失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量获取文件的公开URL（已废弃：OSS bucket已改为"私有写公有读"模式，不再需要签名URL）
     * 保留此接口用于向后兼容，直接返回公开URL列表
     * 
     * @param ids 文件ID列表（逗号分隔）
     * @return 公开URL映射（文件ID -> 公开URL）
     */
    @GetMapping("/signed-urls")
    public Result<Map<Long, String>> getSignedUrls(@RequestParam String ids) {
        try {
            Map<Long, String> urlMap = new HashMap<>();
            
            // 解析ID列表
            String[] idArray = ids.split(",");
            for (String idStr : idArray) {
                try {
                    Long id = Long.parseLong(idStr.trim());
                    
                    // 查询文件记录
                    FileRecord record = fileRecordService.getFileRecordById(id);
                    if (record == null) {
                        continue;
                    }
                    
                    // OSS bucket已改为"私有写公有读"模式，直接返回公开URL
                    // 如果是签名URL，提取基础URL部分（兼容历史数据）
                    String fileUrl = record.getFileUrl();
                    int queryIndex = fileUrl.indexOf('?');
                    if (queryIndex > 0) {
                        String queryString = fileUrl.substring(queryIndex + 1);
                        if (queryString.contains("Expires=") || queryString.contains("Signature=")) {
                            // 从签名URL提取公开URL
                            fileUrl = fileUrl.substring(0, queryIndex);
                        }
                    }
                    
                    urlMap.put(id, fileUrl);
                } catch (NumberFormatException e) {
                    log.warn("无效的文件ID：{}", idStr);
                }
            }
            
            return Result.success(urlMap);
        } catch (Exception e) {
            log.error("批量获取文件URL失败", e);
            return Result.error("批量获取文件URL失败：" + e.getMessage());
        }
    }
    
    /**
     * 格式化文件大小
     * 
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小
     */
    private String formatFileSize(Long size) {
        if (size == null || size == 0) {
            return "0 B";
        }
        
        DecimalFormat df = new DecimalFormat("#.##");
        
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return df.format(size / 1024.0) + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            return df.format(size / (1024.0 * 1024.0)) + " MB";
        } else {
            return df.format(size / (1024.0 * 1024.0 * 1024.0)) + " GB";
        }
    }
}
