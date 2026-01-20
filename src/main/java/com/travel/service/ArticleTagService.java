package com.travel.service;

import com.travel.common.ResultCode;
import com.travel.dto.ArticleTagCreateRequest;
import com.travel.dto.ArticleTagUpdateRequest;
import com.travel.entity.ArticleTag;
import com.travel.exception.BusinessException;
import com.travel.mapper.ArticleTagMapper;
import com.travel.mapper.ArticleTagRelationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文章标签服务类
 * 
 * @author travel-platform
 */
@Slf4j
@Service
public class ArticleTagService {
    
    @Autowired
    private ArticleTagMapper articleTagMapper;
    
    @Autowired
    private ArticleTagRelationMapper articleTagRelationMapper;
    
    /**
     * 查询所有标签列表
     */
    public List<ArticleTag> list() {
        return articleTagMapper.selectAll();
    }
    
    /**
     * 根据ID查询标签详情
     */
    public ArticleTag getById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        
        ArticleTag tag = articleTagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND);
        }
        
        return tag;
    }
    
    /**
     * 创建标签
     */
    @Transactional(rollbackFor = Exception.class)
    public ArticleTag create(ArticleTagCreateRequest request) {
        // 检查标签名称是否已存在
        ArticleTag existingTag = articleTagMapper.selectByName(request.getName());
        if (existingTag != null) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), 
                "标签名称已存在");
        }
        
        // 创建标签实体
        ArticleTag tag = new ArticleTag();
        BeanUtils.copyProperties(request, tag);
        
        // 设置默认值
        if (tag.getUseCount() == null) {
            tag.setUseCount(0);
        }
        
        // 插入数据库
        int result = articleTagMapper.insert(tag);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("创建文章标签成功: id={}, name={}", tag.getId(), tag.getName());
        
        return tag;
    }
    
    /**
     * 更新标签
     */
    @Transactional(rollbackFor = Exception.class)
    public ArticleTag update(Long id, ArticleTagUpdateRequest request) {
        // 检查标签是否存在
        ArticleTag tag = getById(id);
        
        // 如果修改名称，检查新名称是否已存在
        if (request.getName() != null && !request.getName().equals(tag.getName())) {
            ArticleTag existingTag = articleTagMapper.selectByName(request.getName());
            if (existingTag != null) {
                throw new BusinessException(ResultCode.DATA_ALREADY_EXISTS.getCode(), 
                    "标签名称已存在");
            }
        }
        
        // 更新字段
        if (request.getName() != null) {
            tag.setName(request.getName());
        }
        if (request.getColor() != null) {
            tag.setColor(request.getColor());
        }
        
        // 更新数据库
        int result = articleTagMapper.updateById(tag);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("更新文章标签成功: id={}, name={}", tag.getId(), tag.getName());
        
        return tag;
    }
    
    /**
     * 删除标签
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查标签是否存在
        ArticleTag tag = getById(id);
        
        // 检查标签是否被使用
        List<com.travel.entity.ArticleTagRelation> relations = 
            articleTagRelationMapper.selectByTagId(id);
        if (!relations.isEmpty()) {
            throw new BusinessException(ResultCode.OPERATION_FAILED.getCode(), 
                "该标签正在被使用，无法删除");
        }
        
        // 删除标签
        int result = articleTagMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED);
        }
        
        log.info("删除文章标签成功: id={}, name={}", id, tag.getName());
    }
    
    /**
     * 为文章添加标签
     */
    @Transactional(rollbackFor = Exception.class)
    public void addTagToArticle(Long articleId, Long tagId) {
        // 检查标签是否存在
        ArticleTag tag = getById(tagId);
        
        // 检查关联是否已存在
        int count = articleTagRelationMapper.countByArticleIdAndTagId(articleId, tagId);
        if (count > 0) {
            return; // 已存在，直接返回
        }
        
        // 创建关联
        com.travel.entity.ArticleTagRelation relation = new com.travel.entity.ArticleTagRelation();
        relation.setArticleId(articleId);
        relation.setTagId(tagId);
        articleTagRelationMapper.insert(relation);
        
        // 增加标签使用次数
        articleTagMapper.incrementUseCount(tagId);
        
        log.info("为文章添加标签成功: articleId={}, tagId={}", articleId, tagId);
    }
    
    /**
     * 移除文章的标签
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeTagFromArticle(Long articleId, Long tagId) {
        // 删除关联
        int result = articleTagRelationMapper.delete(articleId, tagId);
        if (result > 0) {
            // 减少标签使用次数
            articleTagMapper.decrementUseCount(tagId);
            log.info("移除文章标签成功: articleId={}, tagId={}", articleId, tagId);
        }
    }
}
