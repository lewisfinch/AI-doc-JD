package com.atguigu.java.ai.langchain4j.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenizer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class RagDataInitRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RagDataInitRunner.class);

    @Autowired
    @Qualifier("embeddingStore")
    private EmbeddingStore<TextSegment> embeddingStore;

    @Autowired
    @Qualifier("medicationEmbeddingStore")
    private EmbeddingStore<TextSegment> medicationEmbeddingStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Value("${rag.data.init.enabled:false}")
    private boolean initEnabled;

    @Value("${rag.knowledge.base-path}")
    private String ragKnowledgeBasePath;

    @Value("${rag.knowledge.medication-base-path:}")
    private String medicationKnowledgeBasePath;

    @Override
    public void run(ApplicationArguments args) {
        if (!initEnabled) {
            log.info("RAG 知识库初始化开关已关闭，跳过数据入库。");
            return;
        }

        ingestToStore("通用知识库", ragKnowledgeBasePath, embeddingStore);

        String medicationPath = StringUtils.hasText(medicationKnowledgeBasePath)
            ? medicationKnowledgeBasePath
            : ragKnowledgeBasePath + "/medication";
        ingestToStore("药品知识库", medicationPath, medicationEmbeddingStore);
    }

    private void ingestToStore(String sceneName, String knowledgePath, EmbeddingStore<TextSegment> targetStore) {
        log.info("[{}] 开始从路径 [{}] 加载并解析多格式文档...", sceneName, knowledgePath);

        List<Document> allDocuments = loadDocuments(knowledgePath);
        if (allDocuments.isEmpty()) {
            log.warn("[{}] 未解析到可入库文档，跳过。", sceneName);
            return;
        }

        DocumentByParagraphSplitter documentSplitter = new DocumentByParagraphSplitter(
            300,
            30,
            new HuggingFaceTokenizer()
        );

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(documentSplitter)
            .embeddingModel(embeddingModel)
            .embeddingStore(targetStore)
            .build();

        log.info("[{}] 开始将 {} 份文档切片并向量化入库...", sceneName, allDocuments.size());
        ingestor.ingest(allDocuments);
        log.info("[{}] 文档入库完成。", sceneName);
    }

    private List<Document> loadDocuments(String knowledgePath) {
        List<Document> allDocuments = new ArrayList<>();
        Path dirPath = Paths.get(knowledgePath);

        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            log.warn("知识库路径不存在或不是文件夹: {}", knowledgePath);
            return allDocuments;
        }

        try {
            Files.walk(dirPath)
                .filter(Files::isRegularFile)
                .forEach(filePath -> {
                    String fileName = filePath.getFileName().toString().toLowerCase();
                    try {
                        if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
                            Document doc = FileSystemDocumentLoader.loadDocument(filePath, new TextDocumentParser());
                            allDocuments.add(doc);
                            log.info("成功解析文档: {}", fileName);
                        } else if (fileName.endsWith(".pdf")) {
                            Document doc = FileSystemDocumentLoader.loadDocument(filePath, new ApachePdfBoxDocumentParser());
                            allDocuments.add(doc);
                            log.info("成功解析 PDF: {}", fileName);
                        } else if (!fileName.startsWith(".")) {
                            log.warn("暂不支持格式，已跳过: {}", fileName);
                        }
                    } catch (Exception e) {
                        log.error("解析文件失败: {}", fileName, e);
                    }
                });
        } catch (Exception e) {
            log.error("遍历目录失败: {}", knowledgePath, e);
        }

        return allDocuments;
    }
}