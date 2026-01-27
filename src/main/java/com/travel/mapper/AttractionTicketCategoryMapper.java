package com.travel.mapper;

import com.travel.entity.AttractionTicketCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 票种分类Mapper接口
 * 
 * @author travel-platform
 */
public interface AttractionTicketCategoryMapper {
    
    /**
     * 根据ID查询票种分类
     */
    AttractionTicketCategory selectById(@Param("id") Long id);
    
    /**
     * 查询票种分类列表
     */
    List<AttractionTicketCategory> selectList(@Param("attractionId") Long attractionId);
    
    /**
     * 插入票种分类
     */
    int insert(AttractionTicketCategory category);
    
    /**
     * 更新票种分类信息
     */
    int updateById(AttractionTicketCategory category);
    
    /**
     * 删除票种分类（物理删除）
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据景点ID删除票种分类
     */
    int deleteByAttractionId(@Param("attractionId") Long attractionId);
}
