package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.ArticleCategoryCreateRequest;
import com.travel.dto.ArticleCategoryUpdateRequest;
import com.travel.entity.ArticleCategory;
import com.travel.service.ArticleCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 文章分类管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/article-categories")
@Tag(name = "文章分类管理")
public class ArticleCategoryController {
    
    @Autowired
    private ArticleCategoryService articleCategoryService;
    
    /**
     * 查询所有分类列表
     */
    @GetMapping
    @Operation(summary = "查询所有分类列表")
    public Result<List<ArticleCategory>> list(@RequestParam(required = false) Integer status) {
        List<ArticleCategory> list = articleCategoryService.list(status);
        return Result.success(list);
    }
    
    /**
     * 根据ID查询分类详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询分类详情")
    public Result<ArticleCategory> getById(@PathVariable Long id) {
        ArticleCategory category = articleCategoryService.getById(id);
        return Result.success(category);
    }
    
    /**
     * 创建分类
     */
    @PostMapping
    @Operation(summary = "创建分类")
    public Result<ArticleCategory> create(@Valid @RequestBody ArticleCategoryCreateRequest request) {
        ArticleCategory category = articleCategoryService.create(request);
        return Result.success(category);
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新分类")
    public Result<ArticleCategory> update(@PathVariable Long id, 
                                         @Valid @RequestBody ArticleCategoryUpdateRequest request) {
        ArticleCategory category = articleCategoryService.update(id, request);
        return Result.success(category);
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<?> delete(@PathVariable Long id) {
        articleCategoryService.delete(id);
        return Result.success();
    }
}
