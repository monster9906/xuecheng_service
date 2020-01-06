package com.xuecheng.manage_course.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sun.javafx.runtime.async.BackgroundExecutor;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.cms.response.CoursePublishResult;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseServiceImpl implements CourseService {
    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private CourseBaseRepository courseBaseRepository;

    @Autowired
    private CoursePicRepository coursePicRepository;

    @Autowired
    private CourseMarketRepository courseMarketRepository;

    @Autowired
    private CoursePubRepository coursePubRepository;

    @Autowired
    CmsPageClient cmsPageClient;

   @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;

    @Override
    public QueryResponseResult findCourseList(int page, int size, CourseListRequest courseListRequest) {
        if(courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
        if(page<=0){
            page =1;
        }
        if (size<=0){
            size=20;
        }
        // 设置分页参数
        PageHelper.startPage(page, size);
        // 分页查询
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        // 记录列表
        List<CourseInfo> result = courseListPage.getResult();
        //获取总记录数
        long total = courseListPage.getTotal();
        QueryResult<CourseInfo> courseIncfoQueryResult = new QueryResult<CourseInfo>();
        courseIncfoQueryResult.setTotal(total);
        courseIncfoQueryResult.setList(result);
        return new QueryResponseResult(CommonCode.SUCCESS,courseIncfoQueryResult);
    }

    @Override
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        // 设置课程状态默认为不发布
        courseBase.setStudymodel("201002");
        // 执行新增操作
        CourseBase save = courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    @Override
    public CourseBase getCoursebaseById(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Override
    public ResponseResult updateCoursebase(String id, CourseBase courseBase) {
        CourseBase coursebaseById = this.getCoursebaseById(id);
        if(coursebaseById == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        // 设置课程名称
        coursebaseById.setName(courseBase.getName());
        // 设置课程大分类
        coursebaseById.setMt(courseBase.getMt());
        // 设置课程小分类
        coursebaseById.setSt(courseBase.getSt());
        // 设置课程等级
        coursebaseById.setGrade(courseBase.getGrade());
        // 设置学习模式
        coursebaseById.setStudymodel(courseBase.getStudymodel());
        // 设置适用人群
        coursebaseById.setUsers(courseBase.getUsers());
        // 设置课程介绍
        coursebaseById.setDescription(courseBase.getDescription());
        // 执行修改课程信息
        CourseBase save = courseBaseRepository.save(coursebaseById);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Override
    public ResponseResult addCoursePic(String courseId, String pic) {
        CoursePic coursePic = null;
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if(optional.isPresent()){
            coursePic = optional.get();
        }else{
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        // 执行新增操作
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Override
    public CoursePic findCoursePic(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Override
    public ResponseResult deleteCoursePic(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if(optional.isPresent()){
            coursePic = optional.get();
        }
        coursePicRepository.deleteById(courseId);
        if(coursePic!=null){
            return new ResponseResult(CommonCode.SUCCESS);
        }else {
            return new ResponseResult(CommonCode.FAIL);
        }
    }

    @Override
    public CourseView getCoruseView(String id) {
        CourseView courseView = new CourseView();
        // 查询课程基础信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if(courseBaseOptional.isPresent()){
            courseView.setCourseBase(courseBaseOptional.get());
        }
        // 查询课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if (marketOptional.isPresent()){
            courseView.setCourseMarket(marketOptional.get());
        }
        // 查询课程图片信息
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if (picOptional.isPresent()){
            courseView.setCoursePic(picOptional.get());
        }
        // 查询课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    /**
     * 课程预览页面
     * @param courseId
     * @return
     */
    @Override
    public CoursePublishResult preview(String courseId) {
        CourseBase coursebase = this.getCoursebaseById(courseId);
        CmsPage cmsPage = setValue(coursebase, courseId);
        // 执行远程保存操作
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if(!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        // 页面id
        String pageId = cmsPageResult.getCmsPage().getPageId();
        // 页面URL
        String pageUrl = previewUrl+pageId;
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    @Transactional
    @Override
    public CoursePublishResult publish(String courseId) {
        // 获取课程基础信息
        CourseBase coursebase = this.getCoursebaseById(courseId);
        // 发布课程详情页面
        CmsPostPageResult cmsPostPageResult = publish_page(courseId);
        if (!cmsPostPageResult.isSuccess()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        // 更新课程状态
        CourseBase courseBase1 = saveCoursePubState(courseId);
        // 页面的URL
        String pageUrl = cmsPostPageResult.getPageUrl();

        // 创建课程索引信息
        CoursePub coursePub = createCoursePub(courseId);
        // 向数据库中保存课程索引信息
        CoursePub newCoursePub = saveCoursePub(courseId,coursePub);
        if (newCoursePub == null){
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_CREATE_INDEX_ERROR);
        }
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    /**
     * @Description:向数据库中保存课程索引信息
     * @Author: ChosenOne
     * @return com.xuecheng.framework.domain.course.CoursePub
     * @param courseId
     * @param coursePub
     **/
    private CoursePub saveCoursePub(String courseId, CoursePub coursePub) {
        if (StringUtils.isEmpty(courseId)){
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        CoursePub coursePubNew = null;
        // 根据id查询
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(courseId);
        if(coursePubOptional.isPresent()){
            coursePubNew = coursePubOptional.get();
        }else{
            coursePubNew = new CoursePub();
        }
        // 设置相关属性
        BeanUtils.copyProperties(coursePub,coursePubNew);
        // 设置id
        coursePubNew.setId(courseId);
        // 设置时间戳
        coursePubNew.setTimestamp(new Date());
        // 设置发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY‐MM‐dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(date);
        // 执行新增操作
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }

    /**
     * @Description: 创建课程索引信息
     * @Author: ChosenOne
     * @return com.xuecheng.framework.domain.course.CoursePub
     * @param courseId
     **/
    private CoursePub createCoursePub(String courseId) {
        CoursePub coursePub = new CoursePub();
        coursePub.setId(courseId);
        // 课程基础信息
        Optional<CourseBase> courseBase = courseBaseRepository.findById(courseId);
        if(courseBase.isPresent()){
            BeanUtils.copyProperties(courseBase.get(),coursePub);
        }
        // 课程图片
        Optional<CoursePic> coursePic = coursePicRepository.findById(courseId);
        if(coursePic.isPresent()){
            BeanUtils.copyProperties(coursePic.get(),coursePub);
        }
        // 课程营销信息
        Optional<CourseMarket> courseMarket = courseMarketRepository.findById(courseId);
        if(courseMarket.isPresent()){
            BeanUtils.copyProperties(courseMarket.get(),coursePub);
        }
        // 课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        String jsonString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(jsonString);
        return coursePub;
    }

    /**
     *@descripotion  更新课程发布状态
     *@params  [courseId]
     *@auth  monsterFu
     *@date  2019/12/26
     */
    private CourseBase saveCoursePubState(String courseId) {
        CourseBase coursebaseById = this.getCoursebaseById(courseId);
        // 更新发布状态
        coursebaseById.setStatus("202002");
        // 执行操作
        CourseBase save = courseBaseRepository.save(coursebaseById);
        return save;
    }

    /**
     *@descripotion 发布课程详情页面
     *@params  [courseId]
     *@auth  monsterFu
     *@date  2019/12/26
     */
    private CmsPostPageResult publish_page(String courseId) {
        CourseBase coursebase = this.getCoursebaseById(courseId);
        // 发布课程预览页面
        CmsPage cmsPage = setValue(coursebase, courseId);
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        return cmsPostPageResult;
    }

    private CmsPage setValue(CourseBase coursebase,String courseId){
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//课程预览站点
        cmsPage.setTemplateId(publish_templateId); // 模板id
        cmsPage.setPageName(courseId +".html"); // 页面
        cmsPage.setPageAliase(coursebase.getName()); // 页面别名
        cmsPage.setPageWebPath(publish_page_webpath); // 页面访问路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath); // 页面存贮路径
        cmsPage.setDataUrl(publish_dataUrlPre+courseId); //数据URL
        return cmsPage;
    }


}
