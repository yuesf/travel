package com.travel.service;

import com.travel.entity.FileRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 文件记录服务接口
 * 
 * @author travel-platform
 */
public interface FileRecordService {
    
    /**
     * 保存文件记录
     * @param record 文件记录对象
     * @return 保存后的文件记录
     */
    FileRecord saveFileRecord(FileRecord record);
    
    /**
     * 根据ID查询文件记录
     * @param id 文件ID
     * @return 文件记录对象
     */
    FileRecord getFileRecordById(Long id);
    
    /**
     * 条件查询文件记录列表（分页）
     * @param fileType 文件类型（image/video/all）
     * @param module 模块名称
     * @param storageType 存储类型（OSS/LOCAL/all）
     * @param keyword 搜索关键词
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 文件记录列表
     */
    Map<String, Object> queryFileRecords(
        String fileType,
        String module,
        String storageType,
        String keyword,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int page,
        int pageSize
    );
    
    /**
     * 删除文件记录
     * @param id 文件ID
     * @return true-删除成功，false-删除失败
     */
    boolean deleteFileRecord(Long id);
    
    /**
     * 批量删除文件记录
     * @param ids 文件ID列表
     * @return 成功删除的数量
     */
    int deleteFileRecordsBatch(List<Long> ids);
    
    /**
     * 根据ID列表查询文件记录
     * @param ids 文件ID列表
     * @return 文件记录列表
     */
    List<FileRecord> getFileRecordsByIds(List<Long> ids);
    
    /**
     * 获取文件统计信息
     * @return 统计信息（总文件数、总大小、OSS文件数、OSS大小、本地文件数、本地大小）
     */
    Map<String, Object> getFileStatistics();
}
