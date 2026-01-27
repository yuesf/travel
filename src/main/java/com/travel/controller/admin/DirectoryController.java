package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.entity.Directory;
import com.travel.service.DirectoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 目录管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/directories")
public class DirectoryController {
    
    @Autowired
    private DirectoryService directoryService;
    
    /**
     * 获取目录树
     * 
     * @return 目录树列表
     */
    @GetMapping("/tree")
    public Result<List<Directory>> getDirectoryTree() {
        try {
            List<Directory> tree = directoryService.getDirectoryTree();
            return Result.success(tree);
        } catch (Exception e) {
            log.error("获取目录树失败", e);
            return Result.error("获取目录树失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建目录
     * 
     * @param name 目录名称
     * @param parentId 父目录ID（可选）
     * @return 创建的目录对象
     */
    @PostMapping
    public Result<Directory> createDirectory(
            @RequestParam("name") String name,
            @RequestParam(value = "parentId", required = false) Long parentId) {
        try {
            Directory directory = directoryService.createDirectory(name, parentId);
            return Result.success(directory);
        } catch (IllegalArgumentException e) {
            log.warn("创建目录失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建目录异常", e);
            return Result.error("创建目录失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询目录
     * 
     * @param id 目录ID
     * @return 目录对象
     */
    @GetMapping("/{id}")
    public Result<Directory> getDirectoryById(@PathVariable Long id) {
        try {
            Directory directory = directoryService.getDirectoryById(id);
            if (directory == null) {
                return Result.error("目录不存在");
            }
            return Result.success(directory);
        } catch (Exception e) {
            log.error("查询目录失败", e);
            return Result.error("查询目录失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除目录
     * 
     * @param id 目录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteDirectory(@PathVariable Long id) {
        try {
            boolean success = directoryService.deleteDirectory(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (IllegalArgumentException e) {
            log.warn("删除目录失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除目录异常", e);
            return Result.error("删除目录失败：" + e.getMessage());
        }
    }
}
