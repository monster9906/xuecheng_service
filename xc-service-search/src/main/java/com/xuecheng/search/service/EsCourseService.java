package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description: 课程搜索service
 * @author: ChosenOne
 * @createDate: 2020/1/4
 */
@Service
public class EsCourseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EsCourseService.class);
    @Value("${xuecheng.elasticsearch.course.index}")
    private String es_index;
    @Value("${xuecheng.elasticsearch.course.type}")
    private String es_type;
    @Value("${xuecheng.elasticsearch.course.source_field}")
    private String source_field;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * @Description:课程首页搜索功能
     * @Author: ChosenOne
     * @return com.xuecheng.framework.model.response.QueryResponseResult<com.xuecheng.framework.domain.course.CoursePub>
     * @param page
     * @param size
     * @param courseSearchParam
     **/
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam){
        // 设置索引
        SearchRequest searchRequest = new SearchRequest(es_index);
        // 设置类型
        searchRequest.types(es_type);
        // 搜索对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 布尔查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 设置过滤字段
        String[] source_fields = source_field.split(",");
        searchSourceBuilder.fetchSource(source_fields,new String[]{});
        // 关键字搜索
        if(StringUtils.isNotEmpty(courseSearchParam.getKeyword())){
            // 匹配搜索
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(),"name","description","teachplan")
                    // 设置匹配占比
            .minimumShouldMatch("70%")
                    // 提升name的权重
            .field("name",10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }

        // 按分类和难度等级搜索,过滤
        //一级分类
        if(StringUtils.isNotEmpty(courseSearchParam.getMt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        //二级分类
        if(StringUtils.isNotEmpty(courseSearchParam.getSt())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        // 难度等级
        if(StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }

        // 分页条件查询
        if(page<=0){
            page = 1;
        }
        if(size<=0){
            size = 20;
        }
        int start = (page-1)*size;
        searchSourceBuilder.from(start);
        searchSourceBuilder.size(size);
        // 布尔查询
        searchSourceBuilder.query(boolQueryBuilder);
        // 进行高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        // 设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);

        // 执行请求搜索
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("xuecheng search error..{}",e.getMessage());
            return new QueryResponseResult<>(CommonCode.FAIL,new QueryResult<CoursePub>());
        }
        // 获取相应结果集，并对结果集处理
        SearchHits hits = searchResponse.getHits();
        // 相应的记录
        SearchHit[] searchHits = hits.getHits();
        // 总记录数
        long totalHits = hits.getTotalHits();
        // 数据列表
        List<CoursePub> list = new ArrayList<>();
        for (SearchHit hit:searchHits) {
            CoursePub coursePub = new CoursePub();
            // 取出source中的记录
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // 名称
            String name =  (String) sourceAsMap.get("name");
            // 取出高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(highlightFields != null){
                HighlightField nameField = highlightFields.get("name");
                if (nameField != null){
                    Text[] fragments = nameField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text text:fragments) {
                        stringBuffer.append(text);
                    }
                    name = stringBuffer.toString();
                }
            }
            coursePub.setName(name);

            // 图片
            coursePub.setPic((String) sourceAsMap.get("pic"));
            // 价格
            Double price = null;
            if(sourceAsMap.get("price") != null){
                price = (Double) sourceAsMap.get("price");
            }
            coursePub.setPrice(price);
            Double price_old = null;
            if(sourceAsMap.get("price_old") != null){
                price = (Double) sourceAsMap.get("price_old");
            }
            coursePub.setPrice_old(price_old);
            list.add(coursePub);
        }
        QueryResult<CoursePub> queryResult = new QueryResult<>();
        queryResult.setList(list);
        queryResult.setTotal(totalHits);
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }
}
