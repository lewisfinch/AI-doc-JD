package com.atguigu.java.ai.langchain4j.bean;

/**
 * 用药咨询请求
 */
public class MedicationConsultRequest {

    private String message;

    public String getMessage() {
        return message;
    }

    public MedicationConsultRequest setMessage(String message) {
        this.message = message;
        return this;
    }
}