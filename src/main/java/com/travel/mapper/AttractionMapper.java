package com.travel.mapper;

import com.travel.entity.Attraction;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 景点Mapper接口
 * 
 * @author travel-platform
 */
public interface AttractionMapper {
    
    /**
     * 根据ID查询景点
     */
    Attraction selectById(@Param("id") Long id);
    
    /**
     * 分页查询景点列表
     */
    List<Attraction> selectList(@Param("name") String name,
                                @Param("city") String city,
                                @Param("status") Integer status,
                                @Param("offset") Integer offset,
                                @Param("limit") Integer limit);
    
    /**
     * 统计景点总数
     */
    long count(@Param("name") String name,
               @Param("city") String city,
               @Param("status") Integer status);
    
    /**
     * 插入景点
     */
    int insert(Attraction attraction);
    
    /**
     * 更新景点信息
     */
    int updateById(Attraction attraction);
    
    /**
     * 删除景点（物理删除）
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 检查景点是否有关联订单
     */
    long countOrdersByAttractionId(@Param("attractionId") Long attractionId);
}
