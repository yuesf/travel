package com.travel.mapper;

import com.travel.entity.SyncLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 同步日志Mapper接口
 * 
 * @author travel-platform
 */
public interface SyncLogMapper {
    
    /**
     * 根据ID查询同步日志
     */
    SyncLog selectById(@Param("id") Long id);
    
    /**
     * 分页查询同步日志列表
     */
    List<SyncLog> selectList(@Param("syncType") String syncType,
                             @Param("status") String status,
                             @Param("offset") Integer offset,
                             @Param("limit") Integer limit);
    
    /**
     * 统计同步日志总数
     */
    long count(@Param("syncType") String syncType,
               @Param("status") String status);
    
    /**
     * 插入同步日志
     */
    int insert(SyncLog syncLog);
    
    /**
     * 更新同步日志
     */
    int updateById(SyncLog syncLog);
}
