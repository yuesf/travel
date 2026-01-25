package com.travel.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 缓存刷新任务实体类
 * 映射 cache_refresh_task 表
 * 
 * @author travel-platform
 */
@Data
public class CacheRefreshTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 任务ID（UUID）
     */
    private String taskId;

    /**
     * 缓存类型
     */
    private String cacheType;

    /**
     * 任务状态：PENDING(等待中), RUNNING(执行中), COMPLETED(已完成), FAILED(失败)
     */
    private String status;

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 已处理数量
     */
    private Integer processedCount;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failureCount;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
