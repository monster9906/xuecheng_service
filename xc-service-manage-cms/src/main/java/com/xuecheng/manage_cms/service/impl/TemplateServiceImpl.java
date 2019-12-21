package com.xuecheng.manage_cms.service.impl;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsTemplateResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import com.xuecheng.manage_cms.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TemplateServiceImpl implements TemplateService {
    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;
    /**
     * 分页查询所有
     * @param page
     * @param size
     * @return
     */
    @Override
    public QueryResponseResult findList(int page, int size) {
        if(page <= 0){
            page = 1;
        }
        page = page -1;
        if(size<=0){
            size = 10;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsTemplate> all = cmsTemplateRepository.findAll(pageable);
        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }

    @Override
    public CmsTemplateResult add(CmsTemplate cmsTemplate) {
        // 1.通过唯一索引查询是否存在
        CmsTemplate cmsTemplate1 = cmsTemplateRepository.findBySiteIdAndAndTemplateName(cmsTemplate.getSiteId(), cmsTemplate.getTemplateName());
        // 进行异常判断
        if(cmsTemplate1 !=null){
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        // 新增模板操作
        CmsTemplate save = cmsTemplateRepository.save(cmsTemplate1);
        return new CmsTemplateResult(CommonCode.SUCCESS,save);
    }

    @Override
    public CmsTemplate getById(String id) {
        Optional<CmsTemplate> byId = cmsTemplateRepository.findById(id);
        if (byId.isPresent()){
            return byId.get();
        }
        return null;
    }

    @Override
    public CmsTemplateResult update(String id, CmsTemplate cmsTemplate) {
        // 1.通过id查询
        CmsTemplate cmsTemplate1 = this.getById(id);
        if(cmsTemplate1 != null){
            // 设置站点id
            cmsTemplate1.setSiteId(cmsTemplate.getSiteId());
            // 设置模板文件id
            cmsTemplate1.setTemplateFileId(cmsTemplate.getTemplateFileId());
            // 设置模板名称
            cmsTemplate1.setTemplateName(cmsTemplate.getTemplateName());
            // 设置模板参数
            cmsTemplate1.setTemplateParameter(cmsTemplate.getTemplateParameter());
            CmsTemplate save = cmsTemplateRepository.save(cmsTemplate1);
            if (save != null){
                CmsTemplateResult result = new CmsTemplateResult(CommonCode.SUCCESS,save);
            }
        }
        // 修改失败
        return new CmsTemplateResult(CommonCode.FAIL, null);
    }

    @Override
    public ResponseResult delete(String id) {
        // 1.通过id查询
        CmsTemplate cmsTemplate = this.getById(id);
        if(cmsTemplate !=null){
            cmsTemplateRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }
}
