package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.model.response.QueryResponseResult;

public interface SiteService {

    public QueryResponseResult findList(int page, int size);

}
