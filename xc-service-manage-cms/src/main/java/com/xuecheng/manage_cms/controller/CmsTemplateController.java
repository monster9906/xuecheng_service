package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsTemplateControllerApi;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.response.CmsTemplateResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/cms/template")
public class CmsTemplateController implements CmsTemplateControllerApi {
    @Autowired
    private TemplateService templateService;

    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size") int size) {
        return templateService.findList(page, size);
    }

    @Override
    public CmsTemplateResult add(CmsTemplate cmsTemplate) {
        return templateService.add(cmsTemplate);
    }

    @Override
    public CmsTemplate findById(String id) {
        return templateService.getById(id);
    }

    @Override
    public CmsTemplateResult edit(String id, CmsTemplate cmsTemplate) {
        return templateService.update(id, cmsTemplate);
    }

    @Override
    public ResponseResult delete(String id) {
        return templateService.delete(id);
    }
}
