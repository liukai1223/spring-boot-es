package com.liukai.springbootes;

import com.liukai.springbootes.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

public class ElasticsearchOptionsTests extends SpringBootEsApplicationTests{

private final ElasticsearchOperations elasticsearchOperations;
    @Autowired
    public ElasticsearchOptionsTests(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }


    /**
     *  save 索引一条文档 更新文档
     *  save 方法当文档id  不存在时添加文档，当文档id存在时候跟新文档
     *
     */

    @Test
    public void testIndex(){
        Product product=new Product();
        product.setId(2);
        product.setTitle("日本豆");
        product.setPrice(2.5);
        product.setDescription("日本豆真好吃，曾经非常爱吃");
        elasticsearchOperations.save(product);
    }
    /**
     * 查询一条文档
     */

    @Test
    public void  testSearch(){
        Product product = elasticsearchOperations.get("1", Product.class);
        System.out.println(product);
    }

    /**
     * 删除文档
     */

    @Test
    public void testDelete(){
        Product product=new Product();
        product.setId(1);
        elasticsearchOperations.delete(product);
    }

    /**
     * 删除所有
     */
    @Test
    public void testDeleteAll(){
        elasticsearchOperations.delete(String.valueOf(Query.findAll()),Product.class);
    }

    /**
     * 查询所有
     */

    @Test
    public void testFindAll(){
        SearchHits<Product> search = elasticsearchOperations.search(Query.findAll(), Product.class);

        System.out.println("总分数"+search.getMaxScore());
        System.out.println("符合条件总条数"+search.getTotalHits());
        for (SearchHit<Product> searchHits :search ) {
            System.out.println(searchHits);
        }
    }
}
