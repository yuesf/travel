package com.travel.mapper;

import com.travel.entity.CacheRefreshTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 缓存刷新任务Mapper接口
 * 
 * @author travel-platform
 */
@Mapper
public interface CacheRefreshTaskMapper {

    /**
     * 插入任务
     * 
     * @param task 缓存刷新任务
     * @return 影响行数
     */
    int insert(CacheRefreshTask task);

    /**
     * 根据任务ID查询
     * 
     * @param taskId 任务ID
     * @return 缓存刷新任务
     */
    CacheRefreshTask selectByTaskId(@Param("taskId") String taskId);

    /**
     * 根据主键ID查询
     * 
     * @param id 主键ID
     * @return 缓存刷新任务
     */
    CacheRefreshTask selectById(@Param("id") Long id);

    /**
     * 更新任务状态
     * 
     * @param taskId 任务ID
     * @param status 任务状态
     * @return 影响行数
     */
    int updateStatus(@Param("taskId") String taskId, @Param("status") String status);

    /**
     * 更新任务进度
     * 
     * @param taskId 任务ID
     * @param processedCount 已处理数量
     * @param successCount 成功数量
     * @param failureCount 失败数量
     * @return 影响行数
     */
    int updateProgress(@Param("taskId") String taskId, 
                       @Param("processedCount") Integer processedCount,
                       @Param("successCount") Integer successCount,
                       @Param("failureCount") Integer failureCount);

    /**
     * 更新任务（全量更新）
     * 
     * @param task 缓存刷新任务
     * @return 影响行数
     */
    int update(CacheRefreshTask task);

    /**
     * 查询最近的任务
     * 
     * @param limit 查询数量
     * @return 任务列表
     */
    List<CacheRefreshTask> selectRecent(@Param("limit") int limit);

    /**
     * 根据状态查询任务
     * 
     * @param status 任务状态
     * @return 任务列表
     */
    List<CacheRefreshTask> selectByStatus(@Param("status") String status);
}
