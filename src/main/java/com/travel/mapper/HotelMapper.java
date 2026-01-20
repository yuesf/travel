package com.travel.mapper;

import com.travel.entity.Hotel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 酒店Mapper接口
 * 
 * @author travel-platform
 */
public interface HotelMapper {
    
    /**
     * 根据ID查询酒店
     */
    Hotel selectById(@Param("id") Long id);
    
    /**
     * 分页查询酒店列表
     */
    List<Hotel> selectList(@Param("name") String name,
                          @Param("city") String city,
                          @Param("starLevel") Integer starLevel,
                          @Param("status") Integer status,
                          @Param("offset") Integer offset,
                          @Param("limit") Integer limit);
    
    /**
     * 统计酒店总数
     */
    long count(@Param("name") String name,
               @Param("city") String city,
               @Param("starLevel") Integer starLevel,
               @Param("status") Integer status);
    
    /**
     * 插入酒店
     */
    int insert(Hotel hotel);
    
    /**
     * 更新酒店信息
     */
    int updateById(Hotel hotel);
    
    /**
     * 删除酒店（物理删除）
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 检查酒店是否有关联订单
     */
    long countOrdersByHotelId(@Param("hotelId") Long hotelId);
}
