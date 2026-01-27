package com.travel.mapper;

import com.travel.entity.AttractionBookingDate;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 景点可订日期Mapper接口
 * 
 * @author travel-platform
 */
public interface AttractionBookingDateMapper {
    
    /**
     * 根据ID查询可订日期
     */
    AttractionBookingDate selectById(@Param("id") Long id);
    
    /**
     * 根据景点ID和日期查询
     */
    AttractionBookingDate selectByAttractionIdAndDate(@Param("attractionId") Long attractionId, 
                                                       @Param("bookingDate") LocalDate bookingDate);
    
    /**
     * 查询可订日期列表
     */
    List<AttractionBookingDate> selectList(@Param("attractionId") Long attractionId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
    
    /**
     * 插入可订日期
     */
    int insert(AttractionBookingDate bookingDate);
    
    /**
     * 批量插入可订日期
     */
    int insertBatch(@Param("list") List<AttractionBookingDate> list);
    
    /**
     * 更新可订日期信息
     */
    int updateById(AttractionBookingDate bookingDate);
    
    /**
     * 删除可订日期（物理删除）
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据景点ID删除可订日期
     */
    int deleteByAttractionId(@Param("attractionId") Long attractionId);
}
