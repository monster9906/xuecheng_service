package com.xuecheng.manage_cms.service.impl;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import com.xuecheng.manage_cms.service.PageService;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jcajce.provider.symmetric.TEA;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Handler;

/**
 * @author Administrator
 * @version 1.0
 * @create 2018-09-12 18:32
 **/
@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 页面查询方法
     * @param page 页码，从1开始记数
     * @param size 每页记录数
     * @param queryPageRequest 查询条件
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest){
        if(queryPageRequest == null){
            queryPageRequest = new QueryPageRequest();
        }
        // 定义条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains()); // 页面别名
        // 条件值对象
        CmsPage cmsPage = new CmsPage();
        if(!StringUtils.isEmpty(queryPageRequest.getSiteId())){ // 站点id
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        if(!StringUtils.isEmpty(queryPageRequest.getTemplateId())){ // 模板id
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        if(!StringUtils.isEmpty(queryPageRequest.getPageAliase())){ // 页面别名
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        // 查询条件对象
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);
        //分页参数
        if(page <=0){
            page = 1;
        }
        page = page -1;
        if(size<=0){
            size = 10;
        }
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);
        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());//数据列表
        queryResult.setTotal(all.getTotalElements());//数据总记录数
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    public CmsPageResult add(CmsPage cmsPage){
        // 通过唯一索引查询是否已经存在
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        // 进行异常处理
        if(cmsPage1 != null){
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        // 新增页面操作
        cmsPage.setPageId(null);
        CmsPage save = cmsPageRepository.save(cmsPage);
        return new CmsPageResult(CommonCode.SUCCESS,save);
    }

    @Override
    public CmsPage getById(String id) {
        Optional<CmsPage> byId = cmsPageRepository.findById(id);
        if(byId.isPresent()){
            return byId.get();
        }
        return null;
    }

    @Override
    public CmsPageResult update(String id, CmsPage cmsPage) {
        // 1.通过id查询
        CmsPage one = this.getById(id);
        if(one != null){ // 进行更新操作
            // 更新模板id
            one.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            one.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            one.setPageAliase(cmsPage.getPageAliase());
            // 更新页面名称
            one.setPageName(cmsPage.getPageName());
            //更新访问路径
            one.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            one.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            // 执行更新操作
            CmsPage save = cmsPageRepository.save(one);
            if(save != null){ // 更新成功
                CmsPageResult result = new CmsPageResult(CommonCode.SUCCESS,save);
                return result;
            }
        }
        // 操作失败
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    @Override
    public ResponseResult delete(String id) {
        // 1.通过id查询
        CmsPage cmsPage = this.getById(id);
        if(cmsPage != null){
            // 执行删除
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    @Override
    public String getPageHtml(String pageId) {
        //1.获取页面模型数据
        Map model = this.getModelByPageId(pageId);
        if(model == null){
            //获取页面模型数据为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        // 2.获取页面模板
        String template = this.getTemplateByPageId(pageId);
        if(StringUtils.isEmpty(template)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        // 3.生成静态化页面
        String html = this.generateHtml(template, model);
        if (StringUtils.isEmpty(html)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return html;
    }

    @Override
    public ResponseResult postPage(String pageId) {
        // 1.执行页面静态化
        String pageHtml = getPageHtml(pageId);
        if(!StringUtils.isEmpty(pageHtml)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        // 2.保存静态文件
        CmsPage cmsPage = saveHtml(pageId, pageHtml);
        // 3.发送消息
        return null;
    }

    @Override
    public CmsPageResult save(CmsPage cmsPage) {
        //校验页面是否存在，根据页面名称、站点Id、页面webpath查询
        CmsPage one = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (one != null){
            // 执行更新操作
            return this.update(one.getPageId(), cmsPage);
        } else{
            // 执行新增操作
            return  this.add(cmsPage);
        }
    }

    /**
     * 发送消息
     * @param pageId
     */
    private void sendPostPage(String pageId){
        CmsPage cmsPage = getById(pageId);
        if(cmsPage == null){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        Map<String,String> msgMap = new HashMap<>(16);
        msgMap.put("pageId",pageId);
        //消息内容
        String msg = JSON.toJSONString(msgMap);
        //获取站点id作为routingKey
        String siteId = cmsPage.getSiteId();
        //发布消息
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId, msg);
    }

    /**
     * 保存静态文件到GrildFS中
     * @param pageId
     * @param content
     * @return
     */
    private CmsPage saveHtml(String pageId,String content){
        // 查询页面
        Optional<CmsPage> byId = cmsPageRepository.findById(pageId);
        if(!byId.isPresent()){
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = byId.get();
        //存储之前先删除
        String htmlFileId = cmsPage.getHtmlFileId();
        if(!StringUtils.isEmpty(htmlFileId)){
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }
        try {
            //保存html文件到GridFS
            InputStream inputStream = IOUtils.toInputStream(content, "utf-8");
            ObjectId objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
            // 文件id
            String fileId = objectId.toString();
            //将文件id存储到cmspage中
            cmsPage.setHtmlFileId(fileId);
            cmsPageRepository.save(cmsPage);
            return cmsPage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取页面模型数据
     * @return
     */
    private Map getModelByPageId(String pageId){
        // 查询页面信息
        CmsPage byId = this.getById(pageId);
        if (byId == null){
            // 页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        // 获取出dataUrl
        String dataUrl = byId.getDataUrl();
       // dataUrl = dataUrl.replace("localhost:31200", "XC-SERVICE-MANAGE-CMS");
        if(StringUtils.isEmpty(dataUrl)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }

    /**
     * 获取页面模板
     * @param pageId
     * @return
     */
    private String getTemplateByPageId(String pageId){
        // 查询页面信息
        CmsPage byId = this.getById(pageId);
        if (byId == null){
            // 页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        // 页面模板id
        String templateId = byId.getTemplateId();
        if (StringUtils.isEmpty(templateId)){
            // 页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            // 获取模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //取出模板文件内容
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建gridFsResource，用于获取流对象
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            //获取流中的数据
            try {
                String s = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
                return  s;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 页面静态化
     * @param template
     * @param model
     * @return
     */
    private String generateHtml(String template, Map model){
        try {
        //生成配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //模板加载器
        StringTemplateLoader templateLoader = new StringTemplateLoader();
        templateLoader.putTemplate("template",template);
        //配置模板加载器
        configuration.setTemplateLoader(templateLoader);
        //获取模板
        Template template1 = configuration.getTemplate("template");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template1, model);
        return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
