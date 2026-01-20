package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.ArticleTagCreateRequest;
import com.travel.dto.ArticleTagUpdateRequest;
import com.travel.entity.ArticleTag;
import com.travel.service.ArticleTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 文章标签管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/article-tags")
@Tag(name = "文章标签管理")
public class ArticleTagController {
    
    @Autowired
    private ArticleTagService articleTagService;
    
    /**
     * 查询所有标签列表
     */
    @GetMapping
    @Operation(summary = "查询所有标签列表")
    public Result<List<ArticleTag>> list() {
        List<ArticleTag> list = articleTagService.list();
        return Result.success(list);
    }
    
    /**
     * 根据ID查询标签详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询标签详情")
    public Result<ArticleTag> getById(@PathVariable Long id) {
        ArticleTag tag = articleTagService.getById(id);
        return Result.success(tag);
    }
    
    /**
     * 创建标签
     */
    @PostMapping
    @Operation(summary = "创建标签")
    public Result<ArticleTag> create(@Valid @RequestBody ArticleTagCreateRequest request) {
        ArticleTag tag = articleTagService.create(request);
        return Result.success(tag);
    }
    
    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新标签")
    public Result<ArticleTag> update(@PathVariable Long id, 
                                    @Valid @RequestBody ArticleTagUpdateRequest request) {
        ArticleTag tag = articleTagService.update(id, request);
        return Result.success(tag);
    }
    
    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签")
    public Result<?> delete(@PathVariable Long id) {
        articleTagService.delete(id);
        return Result.success();
    }
    
    /**
     * 为文章添加标签
     */
    @PostMapping("/{tagId}/articles/{articleId}")
    @Operation(summary = "为文章添加标签")
    public Result<?> addTagToArticle(@PathVariable Long articleId, 
                                     @PathVariable Long tagId) {
        articleTagService.addTagToArticle(articleId, tagId);
        return Result.success();
    }
    
    /**
     * 移除文章的标签
     */
    @DeleteMapping("/{tagId}/articles/{articleId}")
    @Operation(summary = "移除文章的标签")
    public Result<?> removeTagFromArticle(@PathVariable Long articleId, 
                                         @PathVariable Long tagId) {
        articleTagService.removeTagFromArticle(articleId, tagId);
        return Result.success();
    }
}
