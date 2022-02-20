package com.liukai.springbootes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liukai.springbootes.entity.Product;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollAction;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RestHighLevClientForObject extends SpringBootEsApplicationTests{

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 将对象放入es中
     */
    @Test
    public void testIndex() throws IOException {
        Product product=new Product();
        product.setId(1);
        product.setTitle("小浣熊干脆面");
        product.setPrice(1.5);
        product.setDescription("小浣熊干脆面真好吃！");

        //录入es中
        IndexRequest indexRequest=new IndexRequest("products");
        indexRequest.id(product.getId().toString())
                .source(new ObjectMapper().writeValueAsString(product), XContentType.JSON);
        IndexResponse index = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(index.status());
    }

    /**
     * 查询出来存放到对象中
     * @throws IOException
     */
    @Test
    public void TestSearch() throws IOException {
        SearchRequest products = new SearchRequest("products");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建高亮器
        HighlightBuilder highlightBuilder=new HighlightBuilder();
        highlightBuilder.requireFieldMatch(false).field("description").preTags("<span style='color:red;'>").postTags("</span>");

        searchSourceBuilder.query(QueryBuilders.termQuery("description","好吃"))
        .from(0)
        .size(30)
        .highlighter(highlightBuilder);
        SearchRequest source = products.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(products, RequestOptions.DEFAULT);

        //获取总条数
        System.out.println("总条数:"+search.getHits().getTotalHits().value);
        System.out.println("最大分数："+search.getHits().getMaxScore());

        //获取结果
        SearchHit[] hits = search.getHits().getHits();
        List<Product> productList=new ArrayList<>();
        for (SearchHit hit:hits) {
            System.out.println(hit.getSourceAsString());
            Product product=new ObjectMapper().readValue(hit.getSourceAsString(),Product.class);
            //处理高亮
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields.containsKey("description")){
              product.setDescription(highlightFields.get("description").fragments()[0].toString());
            }
            productList.add(product);
        }
        for (Product product : productList) {
            System.out.println(product);
        }
    }
}
