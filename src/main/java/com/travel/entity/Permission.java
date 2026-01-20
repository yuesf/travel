package com.travel.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限实体类
 * 
 * @author travel-platform
 */
@Data
public class Permission implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 权限名称
     */
    private String name;
    
    /**
     * 权限代码
     */
    private String code;
    
    /**
     * 权限类型：menu-菜单，button-按钮
     */
    private String type;
    
    /**
     * 父权限ID
     */
    private Long parentId;
    
    /**
     * 路径
     */
    private String path;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
