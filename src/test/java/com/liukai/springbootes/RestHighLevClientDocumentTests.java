package com.liukai.springbootes;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;


public class RestHighLevClientDocumentTests extends SpringBootEsApplicationTests {
    @Resource
    private  RestHighLevelClient restHighLevelClient;


//    @Autowired
//    public RestHighLevClientDocumentTests(RestHighLevelClient restHighLevelClient) {
//        this.restHighLevelClient = restHighLevelClient;
//    }

    /**
     * 索引一条文档
     */

    @Test
    public void testCreate() throws IOException {
        IndexRequest indexRequest=new IndexRequest("products");
        indexRequest.id("1")  //手动指定id
                .source("{\"title\":\"小浣熊\",\"price\":2.0,\"created_at\":\"2021-11-12\",\"description\":\"小浣熊真好吃\"}\n" +
                "\n", XContentType.JSON);
        //参数1：索引对象  参数2：请求匹配对象
        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

    }

    /**
     *更新文档
     */

    @Test
    public void testUpdate() throws IOException {
        UpdateRequest updateRequest=new UpdateRequest("products","1");
        updateRequest.doc("{\n" +
                "    \"title\":\"小浣熊干脆面\"\n" +
                "  }",XContentType.JSON);
        restHighLevelClient.update(updateRequest,RequestOptions.DEFAULT);
    }
    /**
     * 删除文档
     */
    @Test
    public void testDelete() throws IOException {
        restHighLevelClient.delete(new DeleteRequest("products","1"),RequestOptions.DEFAULT);
    }

    /**
     * 基于id查询
     */
    @Test
    public void testQueryById() throws IOException {
        GetRequest getRequest=new GetRequest("products","2");
        GetResponse documentFields = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        System.out.println("id:"+documentFields.getId());
        System.out.println(documentFields.getSourceAsString());
    }


    /**
     * 查询所有
     */
    @Test
    public void testMatchAll() throws IOException {
        SearchRequest searchRequest = new SearchRequest("products");//指定搜索索引
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();//指定条件对象
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());//查询所有
        searchRequest.source(searchSourceBuilder);//指定查询条件
        SearchResponse search = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
        //获取总条数
        System.out.println("总条数:"+search.getHits().getTotalHits().value);
        System.out.println("最大分数："+search.getHits().getMaxScore());

        //获取结果
        SearchHit[] hits = search.getHits().getHits();
        for (SearchHit hit:hits) {
            System.out.println(hit.getSourceAsString());
        }

    }

    /**
     * 不同条件查询 term（关键字查询）
     */
    @Test
    public void testQuery() throws IOException {
        SearchRequest products = new SearchRequest("products");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("description","浣熊"));
        products.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(products, RequestOptions.DEFAULT);
        //获取总条数
        System.out.println("总条数:"+search.getHits().getTotalHits().value);
        System.out.println("最大分数："+search.getHits().getMaxScore());

        //获取结果
        SearchHit[] hits = search.getHits().getHits();
        for (SearchHit hit:hits) {
            System.out.println(hit.getSourceAsString());
        }

    }

    @Test
    public void testQuery01() throws IOException {
        //1.term查询
        query(QueryBuilders.termQuery("description","日本"));
        //2.range 查询
        query(QueryBuilders.rangeQuery("price").gt(0).lte(10));
        //3,prefix 查询
        query(QueryBuilders.prefixQuery("title","小浣熊"));
        //4,wildcard 通配符查询？ 一个字符 *任意多个字符
        query(QueryBuilders.wildcardQuery("title","小浣熊*"));
        //5,ids指定id查询
        query(QueryBuilders.idsQuery().addIds("1").addIds("2"));
        //6,multi_match 多字段查询
        query(QueryBuilders.multiMatchQuery("真好吃","title","description"));

    }

    public void query(QueryBuilder queryBuilder) throws IOException {
        SearchRequest products = new SearchRequest("products");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        products.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(products, RequestOptions.DEFAULT);
        //获取总条数
        System.out.println("总条数:"+search.getHits().getTotalHits().value);
        System.out.println("最大分数："+search.getHits().getMaxScore());

        //获取结果
        SearchHit[] hits = search.getHits().getHits();
        for (SearchHit hit:hits) {
            System.out.println(hit.getSourceAsString());
        }
    }

    /**
     * 分页查询 form 起始位置  size每页记录数
     * 排序
     */
    @Test
    public void formQueryTest() throws IOException {
        SearchRequest products = new SearchRequest("products");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建高亮器
        HighlightBuilder highlightBuilder=new HighlightBuilder();
        highlightBuilder.requireFieldMatch(false).field("description").preTags("<span style='color:red;'>").postTags("</span>");
        highlightBuilder.requireFieldMatch(false).field("description").field("title").preTags("<span style='color:red;'>").postTags("</span>");
        searchSourceBuilder.query(QueryBuilders.termQuery("description","好吃"))
                .from(0)   //起始位置
                .size(2)   //每页显示条数 默认返回10条
                .sort("price", SortOrder.ASC)  //指定字段排序 升序
                .fetchSource(new String[]{},new String[]{"created_at"})   //参数1：包含字段数组  参数2 排除字段数组
                .highlighter(highlightBuilder);
        products.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(products, RequestOptions.DEFAULT);
        //获取总条数
        System.out.println("总条数:"+search.getHits().getTotalHits().value);
        System.out.println("最大分数："+search.getHits().getMaxScore());

        //获取结果
        SearchHit[] hits = search.getHits().getHits();
        for (SearchHit hit:hits) {
            System.out.println(hit.getSourceAsString());
            //获取高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields.containsKey("description")){
                System.out.println("description高亮+"+highlightFields.get("description").fragments()[0]);
            }
            if (highlightFields.containsKey("title")){
                System.out.println("title+"+highlightFields.get("title").fragments()[0]);
            }
        }
    }
    /**
     * query :查询精确查询  查询计算文档得分 并根据文档得分进行返回
     * filter query ： 过滤查询 用来大量数据中筛选出来本地查询相关数据  ，不会计算文档得分
     * 注意：一旦使用query和filterQuery Es先执行filterQuery然后在执行query
     */
    @Test
    public void testFilterQuery() throws IOException {
        SearchRequest searchRequest = new SearchRequest("products");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("description","浣熊"))
        .postFilter(QueryBuilders.rangeQuery("price").gt(0).lte(3));
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //获取总条数
        System.out.println("总条数:"+search.getHits().getTotalHits().value);
        System.out.println("最大分数："+search.getHits().getMaxScore());

        //获取结果
        SearchHit[] hits = search.getHits().getHits();
        for (SearchHit hit:hits) {
            System.out.println(hit.getSourceAsString());
        }
    }

}

