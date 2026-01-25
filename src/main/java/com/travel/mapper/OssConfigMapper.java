package com.travel.mapper;

import com.travel.entity.OssConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * OSS配置Mapper接口
 * 
 * @author travel-platform
 */
@Mapper
public interface OssConfigMapper {
    
    /**
     * 获取OSS配置（单例）
     * @return OSS配置对象，如果不存在返回null
     */
    OssConfig selectOne();
    
    /**
     * 插入OSS配置
     * @param config OSS配置对象
     * @return 影响行数
     */
    int insert(OssConfig config);
    
    /**
     * 更新OSS配置
     * @param config OSS配置对象
     * @return 影响行数
     */
    int update(OssConfig config);
    
    /**
     * 删除OSS配置
     * @return 影响行数
     */
    int delete();
}
