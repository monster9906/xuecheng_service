package com.xuecheng.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @description:
 * @author: monsterFu
 * @createDate: 2019/12/24
 */
@RequestMapping("/freemarker")
@Controller
public class FreemarkerController {
    @Autowired
    private RestTemplate restTemplate;

    // 请求详情页数据
    @RequestMapping("/course")
    public String course(Map<String, Object> map){
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31200/course/courseview/297e7c7c62b888f00162b8a7dec20000", Map.class);
        Map body = forEntity.getBody();
        map.putAll(body);
        return "course";
    }

}
