package com.atguigu.java.ai.langchain4j.bean;

/**
 * 创建问诊会话请求
 */
public class ConsultationSessionCreateRequest {

    private Long userId;
    private Long patientId;
    private String title;

    public Long getUserId() {
        return userId;
    }

    public ConsultationSessionCreateRequest setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Long getPatientId() {
        return patientId;
    }

    public ConsultationSessionCreateRequest setPatientId(Long patientId) {
        this.patientId = patientId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ConsultationSessionCreateRequest setTitle(String title) {
        this.title = title;
        return this;
    }
}