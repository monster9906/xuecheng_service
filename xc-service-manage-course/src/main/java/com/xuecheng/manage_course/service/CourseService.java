package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.cms.response.CoursePublishResult;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface CourseService {
    /**
     * 课程列表分页查询
     * @param page
     * @param size
     * @param courseListRequest
     * @return
     */
    public QueryResponseResult findCourseList(int page, int size, CourseListRequest courseListRequest);

    /**
     * 添加课程
     * @param courseBase
     * @return
     */
    public AddCourseResult addCourseBase(CourseBase courseBase);

    /**
     * 根据课程id查询
     * @param courseId
     * @return
     */
    public CourseBase getCoursebaseById(String courseId);

    /**
     * 修改课程基础信息
     * @param id
     * @param courseBase
     * @return
     */
    public ResponseResult updateCoursebase(String id, CourseBase courseBase);

    /**
     * 新增图片基础信息
     * @param courseId
     * @param pic
     * @return
     */
    public ResponseResult addCoursePic(String courseId,String pic);

    /**
     * 查询课程图片
     * @param courseId
     * @return
     */
    public CoursePic findCoursePic(String courseId);

    /**
     * 删除图片信息
     * @param courseId
     * @return
     */
    public ResponseResult deleteCoursePic(String courseId);

    /**
     * 课程视图查询
     * @param id 课程id
     * @return
     */
    public CourseView getCoruseView(String id);

    CoursePublishResult preview(String id);
}
