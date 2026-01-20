package com.travel.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * 文章标签关联实体类
 * 
 * @author travel-platform
 */
@Data
public class ArticleTagRelation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 文章ID
     */
    private Long articleId;
    
    /**
     * 标签ID
     */
    private Long tagId;
}
