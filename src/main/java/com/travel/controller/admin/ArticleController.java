package com.travel.controller.admin;

import com.travel.common.Result;
import com.travel.dto.ArticleCreateRequest;
import com.travel.dto.ArticleListRequest;
import com.travel.dto.ArticleUpdateRequest;
import com.travel.dto.PageResult;
import com.travel.entity.AdminUser;
import com.travel.entity.Article;
import com.travel.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 文章管理控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController("adminArticleController")
@RequestMapping("/api/v1/admin/articles")
@Tag(name = "文章管理")
public class ArticleController {
    
    @Autowired
    private ArticleService articleService;
    
    /**
     * 分页查询文章列表
     */
    @GetMapping
    @Operation(summary = "分页查询文章列表")
    public Result<PageResult<Article>> list(ArticleListRequest request) {
        PageResult<Article> result = articleService.list(request);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询文章详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询文章详情")
    public Result<Article> getById(@PathVariable Long id) {
        Article article = articleService.getById(id);
        return Result.success(article);
    }
    
    /**
     * 创建文章
     */
    @PostMapping
    @Operation(summary = "创建文章")
    public Result<Article> create(@Valid @RequestBody ArticleCreateRequest request) {
        // 获取当前登录的管理员ID
        Long authorId = getCurrentAdminId();
        Article article = articleService.create(request, authorId);
        return Result.success(article);
    }
    
    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新文章")
    public Result<Article> update(@PathVariable Long id, 
                                 @Valid @RequestBody ArticleUpdateRequest request) {
        Article article = articleService.update(id, request);
        return Result.success(article);
    }
    
    /**
     * 删除文章（软删除，改为已下架）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章")
    public Result<?> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }
    
    /**
     * 批量删除文章
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除文章")
    public Result<?> deleteBatch(@RequestBody List<Long> ids) {
        articleService.deleteBatch(ids);
        return Result.success();
    }
    
    /**
     * 获取当前登录的管理员ID
     */
    private Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminUser) {
            AdminUser adminUser = (AdminUser) authentication.getPrincipal();
            return adminUser.getId();
        }
        return null;
    }
}
