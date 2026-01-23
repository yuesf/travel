package com.travel.mapper;

import com.travel.entity.MapLocation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 地图Mapper接口
 * 
 * @author travel-platform
 */
public interface MapMapper {
    
    /**
     * 根据ID查询地图
     */
    MapLocation selectById(@Param("id") Long id);
    
    /**
     * 查询地图列表（按状态筛选）
     */
    List<MapLocation> selectList(@Param("status") Integer status);
    
    /**
     * 插入地图
     */
    int insert(MapLocation mapLocation);
    
    /**
     * 更新地图信息
     */
    int updateById(MapLocation mapLocation);
    
    /**
     * 删除地图（物理删除）
     */
    int deleteById(@Param("id") Long id);
}
