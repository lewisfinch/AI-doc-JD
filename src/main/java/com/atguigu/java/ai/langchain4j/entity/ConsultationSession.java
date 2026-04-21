package com.atguigu.java.ai.langchain4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问诊会话实体
 */
@TableName("consultation_session")
public class ConsultationSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 当前登录用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 就诊人ID
     */
    @TableField("patient_id")
    private Long patientId;

    /**
     * LangChain4j在MongoDB中的长记忆ID
     */
    @TableField("memory_id")
    private String memoryId;

    /**
     * AI自动生成摘要标题
     */
    @TableField("title")
    private String title;

    /**
     * 会话状态：0-进行中，1-已结束
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public ConsultationSession setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public ConsultationSession setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Long getPatientId() {
        return patientId;
    }

    public ConsultationSession setPatientId(Long patientId) {
        this.patientId = patientId;
        return this;
    }

    public String getMemoryId() {
        return memoryId;
    }

    public ConsultationSession setMemoryId(String memoryId) {
        this.memoryId = memoryId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ConsultationSession setTitle(String title) {
        this.title = title;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public ConsultationSession setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public ConsultationSession setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }
}