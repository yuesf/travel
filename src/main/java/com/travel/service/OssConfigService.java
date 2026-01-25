package com.travel.service;

import com.travel.entity.OssConfig;

/**
 * OSS配置服务接口
 * 
 * @author travel-platform
 */
public interface OssConfigService {
    
    /**
     * 获取OSS配置
     * @return OSS配置对象，如果不存在返回null
     */
    OssConfig getOssConfig();
    
    /**
     * 保存或更新OSS配置
     * @param config OSS配置对象
     * @return 保存后的配置对象
     */
    OssConfig saveOrUpdateOssConfig(OssConfig config);
    
    /**
     * 测试OSS连接
     * @param config OSS配置对象
     * @return true-连接成功，false-连接失败
     */
    boolean testOssConnection(OssConfig config);
    
    /**
     * 删除OSS配置
     */
    void deleteOssConfig();
}
