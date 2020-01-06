package com.xuecheng.search;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @description:
 * @author: ChosenOne
 * @createDate: 2019/12/31
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestIndex {
  @Autowired
  RestHighLevelClient client;

  @Autowired
  RestClient restClient;  

    /**
     * @Description:删除索引库
     * @Author: ChosenOne
     * @Date: 2019/12/31
     * @return void
     **/
    @Test
    public void testDeleteIndex() throws IOException {
      // 删除索引请求对象
      DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("xc_course");
      // 执行删除索引
      DeleteIndexResponse delete = client.indices().delete(deleteIndexRequest);
      // 获取响应结果
      boolean acknowledged = delete.isAcknowledged();
      System.out.println(acknowledged);
    }

    /**
     * @Description: 创建索引库
     * @Author: ChosenOne
     * @Date: 2019/12/31
     * @return void
     * @param
     **/
    @Test
    public void testCreateIndex() throws IOException {
        // 创建索引请求对象
      CreateIndexRequest createIndexRequest = new CreateIndexRequest("xc_course");
      // 设置索引参数
      createIndexRequest.settings(Settings.builder().put("number_of_shards",1).put("number_of_replicas",0));

      // 设置映射
      createIndexRequest.mapping("doc","{\n" +
              "\t\"properties\":{\n" +
              "\t\t\"description\": {\n" +
              "\t\t\t\"type\":\"text\",\n" +
              "\t\t\t\"analyzer\": \"ik_max_word\",\n" +
              "\t\t\t\"search_analyzer\": \"ik_smart\"\n" +
              "\t\t},\n" +
              "\t\t\"name\":{\n" +
              "\t\t\t\"type\":\"text\",\n" +
              "\t\t\t\"analyzer\": \"ik_max_word\",\n" +
              "\t\t\t\"search_analyzer\": \"ik_smart\"\n" +
              "\t\t},\n" +
              "\t\t\"price\":{\n" +
              "\t\t\t\"type\": \"float\"\n" +
              "\t\t},\n" +
              "\t\t\"studymodel\":{\n" +
              "\t\t\t\"type\": \"keyword\"\n" +
              "\t\t}\n" +
              "\t}\n" +
              "\t\n" +
              "}", XContentType.JSON);

       // 创建索引操作客户端
      IndicesClient indices = client.indices();
      // 创建响应对象
      CreateIndexResponse createIndexResponse = indices.create(createIndexRequest);
      // 获取响应结果
      boolean acknowledged = createIndexResponse.isAcknowledged();
      System.out.println(acknowledged);
    }

}
