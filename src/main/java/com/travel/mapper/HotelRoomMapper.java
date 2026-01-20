package com.travel.mapper;

import com.travel.entity.HotelRoom;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 酒店房型Mapper接口
 * 
 * @author travel-platform
 */
public interface HotelRoomMapper {
    
    /**
     * 根据ID查询房型
     */
    HotelRoom selectById(@Param("id") Long id);
    
    /**
     * 根据酒店ID查询房型列表
     */
    List<HotelRoom> selectByHotelId(@Param("hotelId") Long hotelId);
    
    /**
     * 插入房型
     */
    int insert(HotelRoom hotelRoom);
    
    /**
     * 更新房型信息
     */
    int updateById(HotelRoom hotelRoom);
    
    /**
     * 删除房型（物理删除）
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据酒店ID删除所有房型
     */
    int deleteByHotelId(@Param("hotelId") Long hotelId);
    
    /**
     * 检查房型是否有关联订单
     */
    long countOrdersByRoomId(@Param("roomId") Long roomId);
}
