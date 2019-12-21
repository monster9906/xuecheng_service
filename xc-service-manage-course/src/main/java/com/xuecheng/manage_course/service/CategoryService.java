package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.ext.CategoryNode;

public interface CategoryService {

    /**
     * 查询所有分类
     * @return
     */
    public CategoryNode findList();
}
