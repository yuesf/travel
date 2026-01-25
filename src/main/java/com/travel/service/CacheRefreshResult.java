package com.travel.service;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存刷新结果内部类
 * 用于Service层内部传递缓存刷新操作的执行结果
 * 
 * @author travel-platform
 */
@Data
@Builder
public class CacheRefreshResult {

    /**
     * 总数量
     */
    private int totalCount;

    /**
     * 成功数量
     */
    private int successCount;

    /**
     * 失败数量
     */
    private int failedCount;

    /**
     * 失败的ID列表
     */
    @Builder.Default
    private List<Long> failedIds = new ArrayList<>();

    /**
     * 失败原因列表
     */
    @Builder.Default
    private List<String> failedReasons = new ArrayList<>();

    /**
     * 执行耗时（毫秒）
     */
    private long duration;

    /**
     * 判断是否全部成功
     * 
     * @return true如果全部成功，false如果有失败
     */
    public boolean isAllSuccess() {
        return failedCount == 0 && successCount == totalCount;
    }

    /**
     * 计算成功率（百分比）
     * 
     * @return 成功率（0-100），如果总数为0则返回0
     */
    public double getSuccessRate() {
        if (totalCount == 0) {
            return 0.0;
        }
        return (successCount * 100.0) / totalCount;
    }

    /**
     * 添加失败记录
     * 
     * @param id 失败的数据ID
     * @param reason 失败原因
     */
    public void addFailure(Long id, String reason) {
        if (failedIds == null) {
            failedIds = new ArrayList<>();
        }
        if (failedReasons == null) {
            failedReasons = new ArrayList<>();
        }
        if (id != null) {
            failedIds.add(id);
        }
        if (reason != null) {
            failedReasons.add(reason);
        }
        failedCount++;
    }

    /**
     * 创建成功结果
     * 
     * @param totalCount 总数量
     * @param duration 耗时（毫秒）
     * @return 缓存刷新结果
     */
    public static CacheRefreshResult success(int totalCount, long duration) {
        return CacheRefreshResult.builder()
                .totalCount(totalCount)
                .successCount(totalCount)
                .failedCount(0)
                .duration(duration)
                .build();
    }

    /**
     * 创建部分成功结果
     * 
     * @param totalCount 总数量
     * @param successCount 成功数量
     * @param failedCount 失败数量
     * @param duration 耗时（毫秒）
     * @return 缓存刷新结果
     */
    public static CacheRefreshResult partial(int totalCount, int successCount, int failedCount, long duration) {
        return CacheRefreshResult.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failedCount(failedCount)
                .duration(duration)
                .build();
    }
}
