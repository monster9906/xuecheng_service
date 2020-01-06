package com.xuecheng.search;


import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: ChosenOne
 * @createDate: 2019/12/31
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDoc {
    @Autowired
    RestHighLevelClient client;

    @Autowired
    RestClient restClient;

    /**
     * @Description: 添加文档
     * @Author: ChosenOne
     * @Date: 2019/12/31
     * @return void
     * @param
     **/
    @Test
    public void testAddDoc() throws IOException {
        // 准备相关的数据
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "spring cloud实战");
        jsonMap.put("description", "本课程主要从四个章节进行讲解： 1.微服务架构入门 2.spring cloud 基础入门 3.实战Spring Boot 4.注册中心eureka。");
        jsonMap.put("studymodel", "201001");
        SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy‐MM‐dd HH:mm:ss");
        jsonMap.put("timestamp", dateFormat.format(new Date()));
        jsonMap.put("price", 5.6f);

        // 索引请求对象
        IndexRequest indexRequest = new IndexRequest("xc_course","doc");
        // 指定索引文档内容
        indexRequest.source(jsonMap);
        // 响应结果
        IndexResponse index = client.index(indexRequest);
        DocWriteResponse.Result result = index.getResult();
        System.out.println(result);
    }
    
    /**
     * @Description:查询文档
     * @Author: ChosenOne
     * @Date: 2019/12/31
     * @return void
     * @param 
     **/
    @Test
    public void getDoc() throws IOException {
        GetRequest getRequest = new GetRequest("xc_course","doc","WYv6WW8B8DoejQ-xMozw");
        GetResponse documentFields = client.get(getRequest);
        boolean exists = documentFields.isExists();
        Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
        System.out.println(sourceAsMap);
    }

    /**
     * @Description: 更新文档
     * @Author: ChosenOne
     * @Date: 2019/12/31
     * @return void
     * @param
     **/
    @Test
    public void updateDoc() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest("xc_course","doc","WYv6WW8B8DoejQ-xMozw");
        Map<String,String> map = new HashMap<>();
        map.put("name", "spring cloud实战 8888");
        updateRequest.doc(map);
        UpdateResponse update = client.update(updateRequest);
        // 获取响应结果
        RestStatus status = update.status();
        System.out.println(status);
    }

    /**
     * @Description: 删除文档
     * @Author: ChosenOne
     * @Date: 2019/12/31
     * @return void
     * @param
     **/
    @Test
    public void testDelDoc() throws IOException {
        // 删除文档的id
        String id = "WYv6WW8B8DoejQ-xMozw";
       // 删除索引请求对象
        DeleteRequest deleteRequest = new DeleteRequest("xc_course","doc",id);
        // 获取响应结果
        DeleteResponse delete = client.delete(deleteRequest);
        DocWriteResponse.Result result = delete.getResult();
        System.out.println(result);
    }

}
