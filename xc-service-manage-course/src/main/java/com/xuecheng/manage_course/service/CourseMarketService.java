package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.model.response.ResponseResult;

public interface CourseMarketService {
    /**
     * 根据课程id查询课程营销信息
     * @param courseId
     * @return
     */
    public CourseMarket getCourseMarketById(String courseId);

    /**
     * 修改课程营销信息
     * @param id
     * @param courseMarket
     * @return
     */
    public ResponseResult updateCourseMarket(String id,CourseMarket courseMarket);
}
