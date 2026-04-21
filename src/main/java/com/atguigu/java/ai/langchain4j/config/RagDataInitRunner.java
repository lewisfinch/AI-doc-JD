package com.atguigu.java.ai.langchain4j.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.HuggingFaceTokenizer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class RagDataInitRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RagDataInitRunner.class);

    @Autowired
    private EmbeddingStore embeddingStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Value("${rag.data.init.enabled:false}")
    private boolean initEnabled;

    @Value("${rag.knowledge.base-path}")
    private String ragKnowledgeBasePath;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!initEnabled) {
            log.info("RAG 知识库初始化开关已关闭，跳过数据入库。");
            return;
        }

        log.info("开始从路径 [{}] 加载并解析多格式知识库文档...", ragKnowledgeBasePath);

        // 1. 动态遍历并解析多格式文件
        List<Document> allDocuments = new ArrayList<>();
        Path dirPath = Paths.get(ragKnowledgeBasePath);

        if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
            // 使用 Files.walk 遍历目录（支持嵌套子目录）
            Files.walk(dirPath)
                .filter(Files::isRegularFile) // 过滤掉文件夹本身，只处理文件
                .forEach(filePath -> {
                    String fileName = filePath.getFileName().toString().toLowerCase();
                    try {
                        if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
                            // 处理纯文本和 Markdown
                            Document doc = FileSystemDocumentLoader.loadDocument(filePath, new TextDocumentParser());
                            allDocuments.add(doc);
                            log.info("成功解析纯文本文档: {}", fileName);
                        } else if (fileName.endsWith(".pdf")) {
                            // 处理 PDF 文件
                            Document doc = FileSystemDocumentLoader.loadDocument(filePath, new ApachePdfBoxDocumentParser());
                            allDocuments.add(doc);
                            log.info("成功解析 PDF 文档: {}", fileName);
                        } else if (!fileName.startsWith(".")) {
                            // 忽略 Mac 的 .DS_Store 等隐藏文件，提示其他不支持的格式
                            log.warn("暂不支持的文件格式，已跳过: {}", fileName);
                        }
                    } catch (Exception e) {
                        log.error("解析文件失败: {}", fileName, e);
                    }
                });
        } else {
            log.error("配置的知识库路径不存在或不是一个文件夹: {}", ragKnowledgeBasePath);
            return;
        }

        if (allDocuments.isEmpty()) {
            log.warn("在目录中未成功解析到任何支持的文档（.txt, .md, .pdf）！");
            return;
        }

        // 2. 配置文档分割器
        DocumentByParagraphSplitter documentSplitter = new DocumentByParagraphSplitter(
            300,
            30,
            new HuggingFaceTokenizer()
        );

        // 3. 构建 Ingestor 并持久化到 Pinecone
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(documentSplitter)
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build();

        log.info("开始将 {} 份文档切片并向量化存入 Pinecone...", allDocuments.size());
        ingestor.ingest(allDocuments);

        log.info("RAG 知识库多格式文档入库完成！");
    }
}