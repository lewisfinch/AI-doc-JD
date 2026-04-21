package com.atguigu.java.ai.langchain4j.bean;

/**
 * 全科问诊请求
 */
public class DiagnosticChatRequest {

    private String memoryId;
    private String message;

    public String getMemoryId() {
        return memoryId;
    }

    public DiagnosticChatRequest setMemoryId(String memoryId) {
        this.memoryId = memoryId;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public DiagnosticChatRequest setMessage(String message) {
        this.message = message;
        return this;
    }
}