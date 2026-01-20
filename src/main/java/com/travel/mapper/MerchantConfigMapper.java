package com.travel.mapper;

import com.travel.entity.MerchantConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商家配置Mapper接口
 * 
 * @author travel-platform
 */
@Mapper
public interface MerchantConfigMapper {
    
    /**
     * 查询第一条记录
     */
    MerchantConfig selectFirst();
    
    /**
     * 插入
     */
    int insert(MerchantConfig config);
    
    /**
     * 更新
     */
    int updateById(MerchantConfig config);
}
