package com.magese.ai.agent.youcash.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 测试API
 *
 * @author Magese
 * @since 2026/5/14 18:08
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class TestController {


    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;


    @RequestMapping("/test/llm")
    public String testLLM() {
        String content = chatClient.prompt().user("你好，请回复：连接成功").call().content();
        log.info("测试大模型返回：{}", content);
        return content;
    }

    @RequestMapping("/test/embedding")
    public String testEmbedding() {
        float[] emb = embeddingModel.embed("测试向量");
        log.info("测试向量模型，维度={}", emb.length);
        return "维度：" + emb.length;
    }

    @RequestMapping("/test/milvus-write")
    public String testMilvusWrite() {
        Document doc = new Document("Milvus 向量数据库测试", Map.of("type", "test"));
        vectorStore.add(List.of(doc));
        log.info("测试写入Milvus成功");
        return "写入成功";
    }

    @RequestMapping("/test/milvus-search")
    public String testMilvusSearch() {
        List<Document> docs = vectorStore.similaritySearch("向量数据库");
        log.info("测试向量数据库搜索：{}", docs.getFirst().getFormattedContent());
        return docs.isEmpty() ? "未命中" : docs.getFirst().getFormattedContent();
    }
}
