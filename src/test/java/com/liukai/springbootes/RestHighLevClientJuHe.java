package com.liukai.springbootes;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedDoubleTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedMax;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

public class RestHighLevClientJuHe extends SpringBootEsApplicationTests{
    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 基于 terms 类型进行聚合  基于字段进行分组聚合
     * @throws IOException
     */
    @Test
    public void testAggs() throws IOException {
        SearchRequest searchRequest = new SearchRequest("fruit");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(QueryBuilders.matchAllQuery()) //查询条件
                .aggregation(AggregationBuilders.terms("price_group").field("price"))//用来设置聚合处理
                .size(0);
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //处理结果
        Aggregations aggregations = search.getAggregations();
        ParsedDoubleTerms parsedDoubleTerms= aggregations.get("price_group");
        List<? extends Terms.Bucket> buckets = parsedDoubleTerms.getBuckets();
        for (Terms.Bucket bucket:buckets){
            System.out.println(bucket.getKey()+""+bucket.getDocCount());
        }
    }
    /**
     * max(parsedMax) min(parsedMin) sum(parsedSum) avg(parsedAvg)  聚合函数   桶中只有一个返回值
     */

    @Test
    public void testAggsFunction() throws IOException {
        SearchRequest searchRequest = new SearchRequest("fruit");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(QueryBuilders.matchAllQuery())
                //.aggregation(AggregationBuilders.sum("sum_price").field("price"))
                .aggregation(AggregationBuilders.max("max_price").field("price"))
                .size(0);
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        Aggregations aggregations = search.getAggregations();
//        ParsedSum sum_price = aggregations.get("sum_price");
        ParsedMax max_price = aggregations.get("max_price");
        System.out.println(max_price.getValue());



    }

}