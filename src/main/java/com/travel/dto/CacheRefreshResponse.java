package com.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缓存刷新响应DTO
 * 
 * @author travel-platform
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "缓存刷新响应")
public class CacheRefreshResponse {

    /**
     * 缓存类型
     */
    @Schema(description = "缓存类型", example = "product")
    private String cacheType;

    /**
     * 总数量
     */
    @Schema(description = "总数量", example = "100")
    private Integer totalCount;

    /**
     * 成功数量
     */
    @Schema(description = "成功数量", example = "95")
    private Integer successCount;

    /**
     * 失败数量
     */
    @Schema(description = "失败数量", example = "5")
    private Integer failureCount;

    /**
     * 耗时（毫秒）
     */
    @Schema(description = "耗时（毫秒）", example = "1250")
    private Long durationMs;

    /**
     * 任务ID（仅异步执行时返回）
     */
    @Schema(description = "任务ID（仅异步执行时返回）", example = "cache-refresh-20260124-001")
    private String taskId;

    /**
     * 刷新消息
     */
    @Schema(description = "刷新消息", example = "缓存刷新成功")
    private String message;

    /**
     * 创建同步刷新响应
     * 
     * @param cacheType 缓存类型
     * @param totalCount 总数量
     * @param successCount 成功数量
     * @param failureCount 失败数量
     * @param durationMs 耗时（毫秒）
     * @return 缓存刷新响应
     */
    public static CacheRefreshResponse createSyncResponse(String cacheType, Integer totalCount, 
                                                          Integer successCount, Integer failureCount, 
                                                          Long durationMs) {
        return CacheRefreshResponse.builder()
                .cacheType(cacheType)
                .totalCount(totalCount)
                .successCount(successCount)
                .failureCount(failureCount)
                .durationMs(durationMs)
                .message(String.format("缓存刷新完成，成功 %d/%d 项", successCount, totalCount))
                .build();
    }

    /**
     * 创建异步刷新响应
     * 
     * @param cacheType 缓存类型
     * @param totalCount 总数量
     * @param taskId 任务ID
     * @return 缓存刷新响应
     */
    public static CacheRefreshResponse createAsyncResponse(String cacheType, Integer totalCount, String taskId) {
        return CacheRefreshResponse.builder()
                .cacheType(cacheType)
                .totalCount(totalCount)
                .taskId(taskId)
                .message(String.format("异步刷新任务已创建，任务ID: %s", taskId))
                .build();
    }
}
