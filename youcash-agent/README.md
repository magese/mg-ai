# youcash-agent

基于 Spring Boot 3.5 + Spring AI 1.1.5 构建的 AI 代理服务，集成 DeepSeek 大模型与 Milvus 向量数据库，提供 RAG（检索增强生成）智能问答能力。

## 技术架构

| 类别 | 技术选型 |
|------|----------|
| **基础框架** | Spring Boot 3.5.14 |
| **AI 框架** | Spring AI 1.1.5 |
| **聊天模型** | DeepSeek V4 Flash |
| **嵌入模型** | 阿里云 DashScope text-embedding-v4 |
| **向量数据库** | Milvus |
| **Java 版本** | 21 |
| **工具库** | Hutool 5.8.40、Lombok 1.18.38 |

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- Milvus 向量数据库实例

### 配置

配置文件位于 `src/main/resources/application.yml`，主要配置项：

```yaml
server:
  port: 13333

spring:
  ai:
    deepseek:
      api-key: ${DEEPSEEK_API_KEY}          # DeepSeek API 密钥
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-v4-flash           # 聊天模型
    openai:
      embedding:
        api-key: ${DASHSCOPE_API_KEY}        # 阿里云 DashScope API 密钥
        base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
        options:
          model: text-embedding-v4           # 嵌入模型
          dimensions: 1024                   # 向量维度
    vectorstore:
      milvus:
        client:
          host: ${MILVUS_HOST}               # Milvus 主机地址
          port: 19530                        # Milvus 端口
          username: ${MILVUS_USERNAME}       # Milvus 用户名
          password: ${MILVUS_PASSWORD}       # Milvus 密码
        collection-name: youcash_rag_collection  # 集合名称
        embedding-dimension: 1024
        index-type: autoindex
        metric-type: cosine                  # 余弦相似度
        initialize-schema: true
```

> **注意**: 配置文件中的敏感信息（API 密钥、数据库密码等）建议通过环境变量或外部配置中心注入，避免硬编码。

### 启动

```bash
# 编译打包
mvn clean package -pl youcash-agent -am

# 启动服务
java -jar youcash-agent/target/youcash-agent-1.0.0.jar
```

启动成功后，访问 `http://127.0.0.1:13333` 即可使用服务。

## 项目结构

```
youcash-agent/
├── pom.xml                                    # Maven 构建配置
├── README.md                                  # 项目说明文档
└── src/
    ├── main/
    │   ├── java/com/magese/ai/agent/youcash/
    │   │   └── YoucashAgentApp.java           # 应用启动类
    │   └── resources/
    │       └── application.yml                # 应用配置文件
    └── test/
        └── java/                              # 测试代码
```

## 功能说明

- **智能问答**: 基于 DeepSeek V4 Flash 大模型提供自然语言对话能力
- **RAG 检索增强**: 通过 Milvus 向量数据库存储和检索私有知识库，结合大模型生成精准回答
- **文本嵌入**: 使用阿里云 DashScope text-embedding-v4 模型将文本转换为向量表示
