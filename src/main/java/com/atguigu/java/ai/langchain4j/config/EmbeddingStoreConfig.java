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

    @Value("${pinecone.api-key}")
    private String pineconeApiKey;

    @Value("${pinecone.index:xiaozhi-index}")
    private String pineconeIndex;

    @Value("${pinecone.namespace.general:xiaozhi-namespace}")
    private String generalNamespace;

    @Value("${pinecone.namespace.medication:medication-namespace}")
    private String medicationNamespace;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Bean("embeddingStore")
    public EmbeddingStore<TextSegment> embeddingStore() {
        return buildStore(generalNamespace);
    }

    @Bean("medicationEmbeddingStore")
    public EmbeddingStore<TextSegment> medicationEmbeddingStore() {
        return buildStore(medicationNamespace);
    }

    private EmbeddingStore<TextSegment> buildStore(String namespace) {
        return PineconeEmbeddingStore.builder()
            .apiKey(pineconeApiKey)
            .index(pineconeIndex)
            .nameSpace(namespace)
            .createIndex(PineconeServerlessIndexConfig.builder()
                .cloud("AWS")
                .region("us-east-1")
                .dimension(embeddingModel.dimension())
                .build())
            .build();
    }
}