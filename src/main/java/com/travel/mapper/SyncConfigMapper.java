package com.travel.mapper;

import com.travel.entity.SyncConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 同步配置Mapper接口
 * 
 * @author travel-platform
 */
public interface SyncConfigMapper {
    
    /**
     * 根据ID查询同步配置
     */
    SyncConfig selectById(@Param("id") Long id);
    
    /**
     * 根据同步类型查询同步配置
     */
    SyncConfig selectBySyncType(@Param("syncType") String syncType);
    
    /**
     * 查询所有同步配置
     */
    List<SyncConfig> selectAll();
    
    /**
     * 查询启用的同步配置
     */
    List<SyncConfig> selectEnabled();
    
    /**
     * 插入同步配置
     */
    int insert(SyncConfig syncConfig);
    
    /**
     * 更新同步配置
     */
    int updateById(SyncConfig syncConfig);
    
    /**
     * 删除同步配置
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 更新最后同步时间
     */
    int updateLastSyncTime(@Param("id") Long id, @Param("lastSyncTime") java.time.LocalDateTime lastSyncTime);
}
