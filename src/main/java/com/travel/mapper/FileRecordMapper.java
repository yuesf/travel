package com.travel.mapper;

import com.travel.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 文件记录Mapper接口
 * 
 * @author travel-platform
 */
@Mapper
public interface FileRecordMapper {
    
    /**
     * 插入文件记录
     * @param record 文件记录对象
     * @return 影响行数
     */
    int insert(FileRecord record);
    
    /**
     * 根据ID查询文件记录
     * @param id 文件ID
     * @return 文件记录对象
     */
    FileRecord selectById(@Param("id") Long id);
    
    /**
     * 条件查询文件记录列表
     * @param params 查询参数（fileType, module, storageType, keyword, startDate, endDate, offset, limit）
     * @return 文件记录列表
     */
    List<FileRecord> selectByCondition(Map<String, Object> params);
    
    /**
     * 统计符合条件的文件记录数量
     * @param params 查询参数
     * @return 记录数量
     */
    int selectCount(Map<String, Object> params);
    
    /**
     * 删除文件记录
     * @param id 文件ID
     * @return 影响行数
     */
    int delete(@Param("id") Long id);
    
    /**
     * 查询所有文件的总大小
     * @return 总大小（字节）
     */
    Long selectTotalSize();
    
    /**
     * 按存储类型查询文件总大小
     * @param storageType 存储类型（OSS/LOCAL）
     * @return 总大小（字节）
     */
    Long selectSizeByStorageType(@Param("storageType") String storageType);
    
    /**
     * 按存储类型统计文件数量
     * @param storageType 存储类型（OSS/LOCAL）
     * @return 文件数量
     */
    int selectCountByStorageType(@Param("storageType") String storageType);
}
