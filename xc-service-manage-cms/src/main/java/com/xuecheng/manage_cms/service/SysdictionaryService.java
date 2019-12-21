package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;

/**
 * 数据字典service
 */
public interface SysdictionaryService {
    /**
     * 根据类别查询数据字典
     * @param type
     * @return
     */
    public SysDictionary findDictionaryByType(String type);
}
