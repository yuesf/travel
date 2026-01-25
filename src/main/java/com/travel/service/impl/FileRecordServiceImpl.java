package com.travel.service.impl;

import com.travel.entity.FileRecord;
import com.travel.mapper.FileRecordMapper;
import com.travel.service.FileRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件记录服务实现类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class FileRecordServiceImpl implements FileRecordService {
    
    @Autowired
    private FileRecordMapper fileRecordMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileRecord saveFileRecord(FileRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("文件记录对象不能为空");
        }
        
        // 设置创建时间
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(LocalDateTime.now());
        }
        
        fileRecordMapper.insert(record);
        log.info("保存文件记录成功，ID：{}, 文件名：{}", record.getId(), record.getFileName());
        
        return record;
    }
    
    @Override
    public FileRecord getFileRecordById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("文件ID不能为空");
        }
        
        return fileRecordMapper.selectById(id);
    }
    
    @Override
    public Map<String, Object> queryFileRecords(
        String fileType,
        String module,
        String storageType,
        String keyword,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int page,
        int pageSize
    ) {
        // 参数校验
        if (page < 1) {
            page = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }
        
        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("fileType", fileType);
        params.put("module", module);
        params.put("storageType", storageType);
        params.put("keyword", keyword);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("offset", (page - 1) * pageSize);
        params.put("limit", pageSize);
        
        // 查询数据
        List<FileRecord> records = fileRecordMapper.selectByCondition(params);
        int total = fileRecordMapper.selectCount(params);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (int) Math.ceil((double) total / pageSize));
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFileRecord(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("文件ID不能为空");
        }
        
        int rows = fileRecordMapper.delete(id);
        if (rows > 0) {
            log.info("删除文件记录成功，ID：{}", id);
            return true;
        } else {
            log.warn("删除文件记录失败，ID：{}", id);
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteFileRecordsBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("文件ID列表不能为空");
        }
        
        // 过滤掉null值
        List<Long> validIds = ids.stream()
            .filter(id -> id != null)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
        
        if (validIds.isEmpty()) {
            log.warn("没有有效的文件ID");
            return 0;
        }
        
        int rows = fileRecordMapper.deleteBatch(validIds);
        log.info("批量删除文件记录成功，删除数量：{}/{}", rows, validIds.size());
        return rows;
    }
    
    @Override
    public List<FileRecord> getFileRecordsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        // 过滤掉null值
        List<Long> validIds = ids.stream()
            .filter(id -> id != null)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
        
        if (validIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        return fileRecordMapper.selectByIds(validIds);
    }
    
    @Override
    public Map<String, Object> getFileStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 查询总文件数和总大小
        Long totalSize = fileRecordMapper.selectTotalSize();
        
        // 查询OSS文件统计
        Long ossSize = fileRecordMapper.selectSizeByStorageType("OSS");
        int ossCount = fileRecordMapper.selectCountByStorageType("OSS");
        
        // 查询本地文件统计
        Long localSize = fileRecordMapper.selectSizeByStorageType("LOCAL");
        int localCount = fileRecordMapper.selectCountByStorageType("LOCAL");
        
        stats.put("totalCount", ossCount + localCount);
        stats.put("totalSize", totalSize != null ? totalSize : 0L);
        stats.put("ossCount", ossCount);
        stats.put("ossSize", ossSize != null ? ossSize : 0L);
        stats.put("localCount", localCount);
        stats.put("localSize", localSize != null ? localSize : 0L);
        
        return stats;
    }
}
