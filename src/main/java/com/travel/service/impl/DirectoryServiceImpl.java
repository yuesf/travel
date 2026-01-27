package com.travel.service.impl;

import com.travel.entity.Directory;
import com.travel.mapper.DirectoryMapper;
import com.travel.mapper.FileRecordMapper;
import com.travel.service.DirectoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 目录服务实现类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class DirectoryServiceImpl implements DirectoryService {
    
    @Autowired
    private DirectoryMapper directoryMapper;
    
    @Autowired
    private FileRecordMapper fileRecordMapper;
    
    @Override
    public List<Directory> getDirectoryTree() {
        // 查询所有目录
        List<Directory> allDirectories = directoryMapper.selectAll();
        
        if (allDirectories == null || allDirectories.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 构建目录树
        return buildTree(allDirectories);
    }
    
    /**
     * 构建目录树
     */
    private List<Directory> buildTree(List<Directory> allDirectories) {
        // 创建ID到目录的映射
        Map<Long, Directory> directoryMap = new HashMap<>();
        for (Directory dir : allDirectories) {
            directoryMap.put(dir.getId(), dir);
            dir.setChildren(new ArrayList<>());
        }
        
        // 构建树形结构
        List<Directory> rootDirectories = new ArrayList<>();
        for (Directory dir : allDirectories) {
            if (dir.getParentId() == null) {
                // 根目录
                rootDirectories.add(dir);
            } else {
                // 子目录，添加到父目录的children中
                Directory parent = directoryMap.get(dir.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dir);
                }
            }
        }
        
        // 对每个层级的目录进行排序
        sortDirectories(rootDirectories);
        
        return rootDirectories;
    }
    
    /**
     * 递归排序目录
     */
    private void sortDirectories(List<Directory> directories) {
        if (directories == null || directories.isEmpty()) {
            return;
        }
        
        // 按sort和id排序
        directories.sort((a, b) -> {
            int sortCompare = Integer.compare(
                a.getSort() != null ? a.getSort() : 0,
                b.getSort() != null ? b.getSort() : 0
            );
            if (sortCompare != 0) {
                return sortCompare;
            }
            return Long.compare(a.getId(), b.getId());
        });
        
        // 递归排序子目录
        for (Directory dir : directories) {
            if (dir.getChildren() != null && !dir.getChildren().isEmpty()) {
                sortDirectories(dir.getChildren());
            }
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Directory createDirectory(String name, Long parentId) {
        // 验证目录名称
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("目录名称不能为空");
        }
        
        name = name.trim();
        
        // 验证目录名称长度
        if (name.length() > 100) {
            throw new IllegalArgumentException("目录名称不能超过100个字符");
        }
        
        // 验证目录名称格式（不允许包含特殊字符）
        if (!name.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-\\s]+$")) {
            throw new IllegalArgumentException("目录名称只能包含中文、英文、数字、下划线、连字符和空格");
        }
        
        // 检查同一父目录下是否已存在同名目录
        int count = directoryMapper.countByNameAndParentId(name, parentId);
        if (count > 0) {
            throw new IllegalArgumentException("该目录下已存在同名目录");
        }
        
        // 构建目录路径
        String path = buildDirectoryPath(name, parentId);
        
        // 检查路径是否已存在
        Directory existing = directoryMapper.selectByPath(path);
        if (existing != null) {
            throw new IllegalArgumentException("目录路径已存在");
        }
        
        // 计算目录层级
        int level = 1;
        if (parentId != null) {
            Directory parent = directoryMapper.selectById(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("父目录不存在");
            }
            level = parent.getLevel() + 1;
        }
        
        // 创建目录对象
        Directory directory = new Directory();
        directory.setName(name);
        directory.setPath(path);
        directory.setParentId(parentId);
        directory.setLevel(level);
        directory.setSort(0);
        directory.setCreatedAt(LocalDateTime.now());
        // TODO: 设置创建人ID（从SecurityContext获取）
        
        // 保存目录
        directoryMapper.insert(directory);
        
        log.info("创建目录成功，ID：{}, 名称：{}, 路径：{}", directory.getId(), name, path);
        
        return directory;
    }
    
    @Override
    public Directory getDirectoryById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("目录ID不能为空");
        }
        
        return directoryMapper.selectById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDirectory(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("目录ID不能为空");
        }
        
        // 检查目录是否存在
        Directory directory = directoryMapper.selectById(id);
        if (directory == null) {
            throw new IllegalArgumentException("目录不存在");
        }
        
        // 检查目录下是否有文件（包括子目录下的文件）
        int fileCount = fileRecordMapper.countByModulePath(directory.getPath());
        if (fileCount > 0) {
            throw new IllegalArgumentException("该目录或其子目录下存在文件，无法删除。请先删除文件后再删除目录。");
        }
        
        // 递归删除所有子目录
        deleteChildrenRecursively(id);
        
        // 删除当前目录
        int rows = directoryMapper.delete(id);
        if (rows > 0) {
            log.info("删除目录成功，ID：{}, 路径：{}", id, directory.getPath());
            return true;
        } else {
            log.warn("删除目录失败，ID：{}", id);
            return false;
        }
    }
    
    /**
     * 递归删除所有子目录
     * @param parentId 父目录ID
     */
    private void deleteChildrenRecursively(Long parentId) {
        List<Directory> children = directoryMapper.selectByParentId(parentId);
        if (children != null && !children.isEmpty()) {
            for (Directory child : children) {
                // 递归删除子目录的子目录
                deleteChildrenRecursively(child.getId());
                // 删除子目录
                directoryMapper.delete(child.getId());
                log.info("删除子目录，ID：{}, 路径：{}", child.getId(), child.getPath());
            }
        }
    }
    
    @Override
    public String buildDirectoryPath(String name, Long parentId) {
        if (parentId == null) {
            // 根目录，路径就是目录名称
            return name;
        } else {
            // 子目录，路径 = 父目录路径 + "/" + 目录名称
            Directory parent = directoryMapper.selectById(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("父目录不存在");
            }
            return parent.getPath() + "/" + name;
        }
    }
}
