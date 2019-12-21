package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.response.CmsTemplateResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface TemplateService {
    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    public QueryResponseResult findList(int page,int size);

    /**
     * 新增模板
     * @param cmsTemplate
     * @return
     */
    public CmsTemplateResult add(CmsTemplate cmsTemplate);

    /**
     * 根据id查询
     * @param id
     * @return
     */
    public CmsTemplate getById(String id);

    /**
     *
     * @param id
     * @param cmsTemplate
     * @return
     */
    public CmsTemplateResult update(String id,CmsTemplate cmsTemplate);

    /**
     * 根据id删除
     * @param id
     * @return
     */
    public ResponseResult delete(String id);

}
