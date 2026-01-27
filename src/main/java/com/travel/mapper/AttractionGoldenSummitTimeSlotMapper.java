package com.travel.mapper;

import com.travel.entity.AttractionGoldenSummitTimeSlot;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 金顶时间段Mapper接口
 * 
 * @author travel-platform
 */
public interface AttractionGoldenSummitTimeSlotMapper {
    
    /**
     * 根据ID查询时间段
     */
    AttractionGoldenSummitTimeSlot selectById(@Param("id") Long id);
    
    /**
     * 查询时间段列表
     */
    List<AttractionGoldenSummitTimeSlot> selectList(@Param("attractionId") Long attractionId,
                                                    @Param("bookingDate") LocalDate bookingDate);
    
    /**
     * 插入时间段
     */
    int insert(AttractionGoldenSummitTimeSlot timeSlot);
    
    /**
     * 更新时间段信息
     */
    int updateById(AttractionGoldenSummitTimeSlot timeSlot);
    
    /**
     * 删除时间段（物理删除）
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据景点ID删除时间段
     */
    int deleteByAttractionId(@Param("attractionId") Long attractionId);
}
