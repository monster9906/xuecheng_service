package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface TeachplanService {
    public TeachplanNode findTeachplanList(String courseId);

    /**
     * 新增课程计划
     * @param teachplan
     * @return
     */
    ResponseResult addTeachplan(Teachplan teachplan);


}
