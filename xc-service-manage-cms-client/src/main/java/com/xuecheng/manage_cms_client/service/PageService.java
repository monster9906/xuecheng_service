package com.xuecheng.manage_cms_client.service;

public interface PageService {
    /**
     * 将页面html保存到页面物理路径
     * @param pageId
     */
    public void savePageToServerPath(String pageId);
}
