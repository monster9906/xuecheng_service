package com.xuecheng.framework.domain.cms.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:页面发布成功cms返回页面的url
 * @author: monsterFu
 * @createDate: 2019/12/26
 */
@Data
@NoArgsConstructor
public class CmsPostPageResult extends ResponseResult {
    String pageUrl;
    public CmsPostPageResult (ResultCode resultCode, String pageUrl){
        super(resultCode);
        this.pageUrl = pageUrl;
    }
}
