package com.travel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.ResultCode;
import com.travel.entity.CacheRefreshTask;
import com.travel.exception.BusinessException;
import com.travel.mapper.CacheRefreshTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 缓存刷新任务管理服务类
 * 管理异步缓存刷新任务的生命周期
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class CacheRefreshTaskService {

    @Autowired
    private CacheRefreshTaskMapper cacheRefreshTaskMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 创建缓存刷新任务
     * 
     * @param cacheType 缓存类型
     * @param totalCount 总数量
     * @return 任务对象
     */
    @Transactional
    public CacheRefreshTask create(String cacheType, Integer totalCount) {
        try {
            // 生成唯一的任务ID
            String taskId = "cache-refresh-" + UUID.randomUUID().toString();
            
            // 创建任务对象
            CacheRefreshTask task = new CacheRefreshTask();
            task.setTaskId(taskId);
            task.setCacheType(cacheType);
            task.setStatus("PENDING");
            task.setTotalCount(totalCount != null ? totalCount : 0);
            task.setProcessedCount(0);
            task.setSuccessCount(0);
            task.setFailureCount(0);
            task.setStartTime(LocalDateTime.now());
            
            // 插入数据库
            cacheRefreshTaskMapper.insert(task);
            
            log.info("创建缓存刷新任务成功，taskId: {}, cacheType: {}, totalCount: {}", 
                    taskId, cacheType, totalCount);
            
            return task;
            
        } catch (Exception e) {
            log.error("创建缓存刷新任务失败，cacheType: {}", cacheType, e);
            throw new RuntimeException("创建缓存刷新任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据任务ID查询任务
     * 
     * @param taskId 任务ID
     * @return 任务对象
     */
    public CacheRefreshTask getByTaskId(String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "任务ID不能为空");
        }
        
        CacheRefreshTask task = cacheRefreshTaskMapper.selectByTaskId(taskId);
        
        if (task == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "任务不存在: " + taskId);
        }
        
        return task;
    }

    /**
     * 更新任务状态
     * 
     * @param taskId 任务ID
     * @param status 任务状态
     */
    @Transactional
    public void updateStatus(String taskId, String status) {
        try {
            // 验证状态值
            if (!isValidStatus(status)) {
                throw new IllegalArgumentException("无效的任务状态: " + status);
            }
            
            // 更新状态
            int updated = cacheRefreshTaskMapper.updateStatus(taskId, status);
            
            if (updated == 0) {
                log.warn("更新任务状态失败，任务可能不存在，taskId: {}", taskId);
            } else {
                log.info("更新任务状态成功，taskId: {}, status: {}", taskId, status);
            }
            
        } catch (Exception e) {
            log.error("更新任务状态失败，taskId: {}, status: {}", taskId, status, e);
            throw new RuntimeException("更新任务状态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新任务进度
     * 
     * @param taskId 任务ID
     * @param processedCount 已处理数量
     * @param successCount 成功数量
     * @param failureCount 失败数量
     */
    @Transactional
    public void updateProgress(String taskId, Integer processedCount, Integer successCount, Integer failureCount) {
        try {
            int updated = cacheRefreshTaskMapper.updateProgress(taskId, processedCount, successCount, failureCount);
            
            if (updated == 0) {
                log.warn("更新任务进度失败，任务可能不存在，taskId: {}", taskId);
            } else {
                log.info("更新任务进度成功，taskId: {}, processed: {}, success: {}, failure: {}", 
                        taskId, processedCount, successCount, failureCount);
            }
            
        } catch (Exception e) {
            log.error("更新任务进度失败，taskId: {}", taskId, e);
            throw new RuntimeException("更新任务进度失败: " + e.getMessage(), e);
        }
    }

    /**
     * 完成任务
     * 
     * @param taskId 任务ID
     * @param result 刷新结果
     */
    @Transactional
    public void complete(String taskId, CacheRefreshResult result) {
        try {
            CacheRefreshTask task = getByTaskId(taskId);
            
            // 更新任务信息
            task.setStatus("COMPLETED");
            task.setTotalCount(result.getTotalCount());
            task.setProcessedCount(result.getTotalCount());
            task.setSuccessCount(result.getSuccessCount());
            task.setFailureCount(result.getFailedCount());
            task.setEndTime(LocalDateTime.now());
            
            // 保存失败详情（如果有）
            if (result.getFailedIds() != null && !result.getFailedIds().isEmpty()) {
                try {
                    String failedDetails = objectMapper.writeValueAsString(result.getFailedIds());
                    task.setErrorMessage("失败ID列表: " + failedDetails);
                } catch (Exception e) {
                    log.error("序列化失败详情失败", e);
                }
            }
            
            cacheRefreshTaskMapper.update(task);
            
            log.info("任务完成，taskId: {}, 成功: {}/{}, 耗时: {}ms", 
                    taskId, result.getSuccessCount(), result.getTotalCount(), result.getDuration());
            
        } catch (Exception e) {
            log.error("完成任务失败，taskId: {}", taskId, e);
            throw new RuntimeException("完成任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 任务失败
     * 
     * @param taskId 任务ID
     * @param error 错误信息
     */
    @Transactional
    public void fail(String taskId, String error) {
        try {
            CacheRefreshTask task = getByTaskId(taskId);
            
            task.setStatus("FAILED");
            task.setErrorMessage(error);
            task.setEndTime(LocalDateTime.now());
            
            cacheRefreshTaskMapper.update(task);
            
            log.error("任务失败，taskId: {}, error: {}", taskId, error);
            
        } catch (Exception e) {
            log.error("设置任务失败状态异常，taskId: {}", taskId, e);
            throw new RuntimeException("设置任务失败状态异常: " + e.getMessage(), e);
        }
    }

    /**
     * 取消任务
     * 
     * @param taskId 任务ID
     * @return 是否成功取消
     */
    @Transactional
    public boolean cancel(String taskId) {
        try {
            CacheRefreshTask task = getByTaskId(taskId);
            
            // 只能取消 PENDING 或 RUNNING 状态的任务
            if ("COMPLETED".equals(task.getStatus()) || "FAILED".equals(task.getStatus())) {
                log.warn("任务已完成或失败，无法取消，taskId: {}, status: {}", taskId, task.getStatus());
                return false;
            }
            
            // 更新为失败状态
            task.setStatus("FAILED");
            task.setErrorMessage("任务已被取消");
            task.setEndTime(LocalDateTime.now());
            
            cacheRefreshTaskMapper.update(task);
            
            log.info("取消任务成功，taskId: {}", taskId);
            return true;
            
        } catch (Exception e) {
            log.error("取消任务失败，taskId: {}", taskId, e);
            return false;
        }
    }

    /**
     * 查询最近的任务列表
     * 
     * @param limit 查询数量
     * @return 任务列表
     */
    public List<CacheRefreshTask> listRecent(int limit) {
        try {
            return cacheRefreshTaskMapper.selectRecent(limit);
        } catch (Exception e) {
            log.error("查询最近任务列表失败", e);
            throw new RuntimeException("查询最近任务列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证状态值是否有效
     * 
     * @param status 状态值
     * @return 是否有效
     */
    private boolean isValidStatus(String status) {
        return "PENDING".equals(status) || 
               "RUNNING".equals(status) || 
               "COMPLETED".equals(status) || 
               "FAILED".equals(status);
    }
}
