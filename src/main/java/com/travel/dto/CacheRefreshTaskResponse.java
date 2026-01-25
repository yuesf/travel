package com.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 缓存刷新任务响应DTO
 * 用于异步任务的状态查询
 * 
 * @author travel-platform
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "缓存刷新任务响应")
public class CacheRefreshTaskResponse {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID", example = "cache-refresh-20260124-001")
    private String taskId;

    /**
     * 缓存类型
     */
    @Schema(description = "缓存类型", example = "product")
    private String cacheType;

    /**
     * 任务状态
     * PENDING: 等待中
     * RUNNING: 执行中
     * COMPLETED: 已完成
     * FAILED: 失败
     */
    @Schema(description = "任务状态", example = "RUNNING", 
            allowableValues = {"PENDING", "RUNNING", "COMPLETED", "FAILED"})
    private String status;

    /**
     * 总数量
     */
    @Schema(description = "总数量", example = "100")
    private Integer totalCount;

    /**
     * 已处理数量
     */
    @Schema(description = "已处理数量", example = "45")
    private Integer processedCount;

    /**
     * 成功数量
     */
    @Schema(description = "成功数量", example = "43")
    private Integer successCount;

    /**
     * 失败数量
     */
    @Schema(description = "失败数量", example = "2")
    private Integer failureCount;

    /**
     * 进度百分比（0-100）
     */
    @Schema(description = "进度百分比", example = "45")
    private Integer progressPercentage;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间", example = "2026-01-24T12:00:00")
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间", example = "2026-01-24T12:05:30")
    private LocalDateTime endTime;

    /**
     * 耗时（毫秒）
     */
    @Schema(description = "耗时（毫秒）", example = "330000")
    private Long durationMs;

    /**
     * 错误信息（如果失败）
     */
    @Schema(description = "错误信息（如果失败）", example = "连接数据库失败")
    private String errorMessage;

    /**
     * 计算进度百分比
     * 
     * @param processedCount 已处理数量
     * @param totalCount 总数量
     * @return 进度百分比（0-100）
     */
    public static Integer calculateProgress(Integer processedCount, Integer totalCount) {
        if (totalCount == null || totalCount == 0) {
            return 0;
        }
        if (processedCount == null) {
            return 0;
        }
        return (int) ((processedCount * 100.0) / totalCount);
    }

    /**
     * 设置进度（自动计算百分比）
     * 
     * @param processedCount 已处理数量
     * @param totalCount 总数量
     */
    public void setProgress(Integer processedCount, Integer totalCount) {
        this.processedCount = processedCount;
        this.totalCount = totalCount;
        this.progressPercentage = calculateProgress(processedCount, totalCount);
    }
}
