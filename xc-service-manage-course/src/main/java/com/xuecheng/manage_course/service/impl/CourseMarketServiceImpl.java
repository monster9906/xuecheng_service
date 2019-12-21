package com.xuecheng.manage_course.service.impl;

import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.CourseMarketRepository;
import com.xuecheng.manage_course.service.CourseMarketService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @description:
 * @author: monsterFu
 * @createDate: 2019/12/19
 */
@Service
public class CourseMarketServiceImpl implements CourseMarketService {
    @Autowired
    private CourseMarketRepository courseMarketRepository;

    @Override
    public CourseMarket getCourseMarketById(String courseId) {
        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Override
    public ResponseResult updateCourseMarket(String id, CourseMarket courseMarket) {
        CourseMarket courseMarketById = getCourseMarketById(id);
        if(courseMarketById == null){ // 添加操作
            courseMarketById = new CourseMarket();
            courseMarketById.setId(id);
            BeanUtils.copyProperties(courseMarket, courseMarketById);
            // 执行添加操作
            courseMarketRepository.save(courseMarketById);
        }else{ // 修改操作
            //设置收费规则
            courseMarketById.setCharge(courseMarket.getCharge());
            //设置有效性
            courseMarketById.setValid(courseMarket.getValid());
            // 设置咨询QQ
            courseMarketById.setQq(courseMarket.getQq());
            // 设置价格
            courseMarketById.setPrice(courseMarket.getPrice());
            // 课程有效期-开始时间
            courseMarketById.setStartTime(courseMarket.getStartTime());
            // 课程有效期-结束时间
            courseMarketById.setEndTime(courseMarket.getEndTime());
            // 执行修改操作
            courseMarketRepository.save(courseMarketById);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
