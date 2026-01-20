package com.travel.controller.miniprogram;

import com.travel.common.Result;
import com.travel.common.ResultCode;
import com.travel.dto.PageResult;
import com.travel.entity.Article;
import com.travel.service.ArticleService;
import com.travel.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序文章控制器
 * 
 * @author travel-platform
 */
@Slf4j
@RestController("miniprogramArticleController")
@RequestMapping("/api/v1/miniprogram/articles")
@Tag(name = "小程序文章")
public class ArticleController {
    
    @Autowired
    private ArticleService articleService;
    
    /**
     * 获取文章列表（支持分类、标签筛选，排序，分页）
     */
    @GetMapping
    @Operation(summary = "获取文章列表")
    public Result<PageResult<Article>> getArticleList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageResult<Article> result = articleService.listForMiniProgram(
            categoryId, tagId, keyword, sortType, page, pageSize
        );
        return Result.success(result);
    }
    
    /**
     * 获取文章详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情")
    public Result<Article> getArticleDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        Article article = articleService.getDetailForMiniProgram(id, userId);
        return Result.success(article);
    }
    
    /**
     * 搜索文章
     */
    @GetMapping("/search")
    @Operation(summary = "搜索文章")
    public Result<PageResult<Article>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageResult<Article> result = articleService.searchArticles(keyword, page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 获取推荐文章列表
     */
    @GetMapping("/recommend")
    @Operation(summary = "获取推荐文章列表")
    public Result<List<Article>> getRecommendArticles(
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<Article> list = articleService.getRecommendList(limit);
        return Result.success(list);
    }
    
    /**
     * 点赞文章
     */
    @PostMapping("/{id}/like")
    @Operation(summary = "点赞文章")
    public Result<?> likeArticle(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        articleService.likeArticle(id, userId);
        return Result.success("点赞成功");
    }
    
    /**
     * 取消点赞
     */
    @DeleteMapping("/{id}/like")
    @Operation(summary = "取消点赞")
    public Result<?> unlikeArticle(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        articleService.unlikeArticle(id, userId);
        return Result.success("取消点赞成功");
    }
    
    /**
     * 收藏文章
     */
    @PostMapping("/{id}/favorite")
    @Operation(summary = "收藏文章")
    public Result<?> favoriteArticle(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        articleService.favoriteArticle(id, userId);
        return Result.success("收藏成功");
    }
    
    /**
     * 取消收藏
     */
    @DeleteMapping("/{id}/favorite")
    @Operation(summary = "取消收藏")
    public Result<?> unfavoriteArticle(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        articleService.unfavoriteArticle(id, userId);
        return Result.success("取消收藏成功");
    }
    
    /**
     * 获取相关文章推荐
     */
    @GetMapping("/{id}/related")
    @Operation(summary = "获取相关文章推荐")
    public Result<List<Article>> getRelatedArticles(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") Integer limit) {
        
        List<Article> list = articleService.getRelatedArticles(id, limit);
        return Result.success(list);
    }
    
    /**
     * 获取用户收藏的文章列表
     */
    @GetMapping("/favorites")
    @Operation(summary = "获取用户收藏的文章列表")
    public Result<PageResult<Article>> getFavoriteArticles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtils.getCurrentMiniprogramUserId();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        PageResult<Article> result = articleService.getUserFavoriteArticles(userId, page, pageSize);
        return Result.success(result);
    }
}
