package com.travel.mapper;

import com.travel.entity.AttractionTicket;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 具体票种Mapper接口
 * 
 * @author travel-platform
 */
public interface AttractionTicketMapper {
    
    /**
     * 根据ID查询票种
     */
    AttractionTicket selectById(@Param("id") Long id);
    
    /**
     * 查询票种列表
     */
    List<AttractionTicket> selectList(@Param("attractionId") Long attractionId,
                                      @Param("categoryId") Long categoryId);
    
    /**
     * 插入票种
     */
    int insert(AttractionTicket ticket);
    
    /**
     * 更新票种信息
     */
    int updateById(AttractionTicket ticket);
    
    /**
     * 删除票种（物理删除）
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据景点ID删除票种
     */
    int deleteByAttractionId(@Param("attractionId") Long attractionId);
    
    /**
     * 根据分类ID删除票种
     */
    int deleteByCategoryId(@Param("categoryId") Long categoryId);
}
