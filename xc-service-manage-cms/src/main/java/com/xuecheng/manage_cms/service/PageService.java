package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface PageService {

    /**
     * 分页查询
     * @param page
     * @param size
     * @param queryPageRequest
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

    /**
     * 新增页面操作
     * @param cmsPage
     * @return
     */
    public CmsPageResult add(CmsPage cmsPage);

    /**
     * 通过id获取页面
     * @param id
     * @return
     */
    public CmsPage getById(String id);

    /**
     * 修改页面
     * @param id
     * @param cmsPage
     * @return
     */
    public CmsPageResult update(String id,CmsPage cmsPage);

    /**
     * 通过id删除页面
     * @param id
     * @return
     */
    public ResponseResult delete(String id);

    /**
     * 页面静态化方法
     * @param pageId
     * @return
     */
    public String getPageHtml(String pageId);

    /**
     * 页面发布
     * @param pageId
     * @return
     */
    public ResponseResult postPage(String pageId);

    /**
     * 保存页面
     * @param cmsPage
     * @return
     */
    CmsPageResult save(CmsPage cmsPage);
}
