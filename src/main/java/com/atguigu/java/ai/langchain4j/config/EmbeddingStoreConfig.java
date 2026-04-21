package com.atguigu.java.ai.langchain4j.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeServerlessIndexConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingStoreConfig {

    // 统一通过 Spring 注入配置文件中的值
    @Value("${pinecone.api-key}")
    private String pineconeApiKey;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return PineconeEmbeddingStore.builder()
            .apiKey(pineconeApiKey) // 替换掉 System.getenv
            .index("xiaozhi-index")
            .nameSpace("xiaozhi-namespace")
            .createIndex(PineconeServerlessIndexConfig.builder()
                .cloud("AWS")
                .region("us-east-1")
                .dimension(embeddingModel.dimension())
                .build())
            .build();
    }
}