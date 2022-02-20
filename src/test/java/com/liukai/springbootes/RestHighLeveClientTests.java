package com.liukai.springbootes;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.io.IOException;

public class RestHighLeveClientTests extends SpringBootEsApplicationTests {
    @Resource
    private  RestHighLevelClient restHighLevelClient;


    /**
     * 创建索引
     */
    @Test
    public void testIndex() throws IOException {
        //参数1：创建索引对象  参数2：请求配置对象
        CreateIndexRequest createIndexRequest=new CreateIndexRequest("products");
        createIndexRequest.mapping("{\n" +
                "    \"properties\": {\n" +
                "      \"title\":{\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"price\":{\n" +
                "        \"type\": \"double\"\n" +
                "      },\n" +
                "      \"created_at\":{\n" +
                "        \"type\": \"date\"\n" +
                "      },\n" +
                "      \"description\":{\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"ik_max_word\"\n" +
                "      }\n" +
                "    }\n" +
                "  }", XContentType.JSON);//指定映射 参数1：指定映射json解构  参数2：指定数据类型
        CreateIndexResponse createIndexResponse=restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        System.out.println("创建状态："+createIndexResponse.isAcknowledged());
        restHighLevelClient.close();//关闭资源
    }

    /**
     * 删除索引
     */

    @Test
    public void testDeleteIndex() throws IOException {
        //参数1：删除索引对象  参数2：请求配置对象
        AcknowledgedResponse acknowledgedResponse=restHighLevelClient.indices().delete(new DeleteIndexRequest("products"),RequestOptions.DEFAULT);
        System.out.println(acknowledgedResponse.isAcknowledged());

    }

}
