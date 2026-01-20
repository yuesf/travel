package com.travel.mapper;

import com.travel.entity.MiniProgramConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 小程序配置Mapper接口
 * 
 * @author travel-platform
 */
public interface MiniProgramConfigMapper {
    
    /**
     * 根据ID查询配置
     */
    MiniProgramConfig selectById(@Param("id") Long id);
    
    /**
     * 根据配置键查询配置
     */
    MiniProgramConfig selectByConfigKey(@Param("configKey") String configKey);
    
    /**
     * 根据配置类型查询配置列表
     */
    List<MiniProgramConfig> selectByConfigType(@Param("configType") String configType,
                                              @Param("status") Integer status);
    
    /**
     * 查询所有配置列表
     */
    List<MiniProgramConfig> selectAll(@Param("configType") String configType,
                                     @Param("status") Integer status);
    
    /**
     * 插入配置
     */
    int insert(MiniProgramConfig config);
    
    /**
     * 更新配置信息
     */
    int updateById(MiniProgramConfig config);
    
    /**
     * 删除配置（物理删除）
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据配置键删除配置
     */
    int deleteByConfigKey(@Param("configKey") String configKey);
}
