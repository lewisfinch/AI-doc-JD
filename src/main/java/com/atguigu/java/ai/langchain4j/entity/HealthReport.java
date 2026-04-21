package com.atguigu.java.ai.langchain4j.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 健康报告实体
 */
@TableName("health_report")
public class HealthReport implements Serializable {

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
     * 上传文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 报告类型：1-体检，2-化验，3-影像
     */
    @TableField("report_type")
    private Integer reportType;

    /**
     * 大模型分析结论
     */
    @TableField("analysis_result")
    private String analysisResult;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public HealthReport setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public HealthReport setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Long getPatientId() {
        return patientId;
    }

    public HealthReport setPatientId(Long patientId) {
        this.patientId = patientId;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public HealthReport setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public HealthReport setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
        return this;
    }

    public Integer getReportType() {
        return reportType;
    }

    public HealthReport setReportType(Integer reportType) {
        this.reportType = reportType;
        return this;
    }

    public String getAnalysisResult() {
        return analysisResult;
    }

    public HealthReport setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
        return this;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public HealthReport setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
        return this;
    }
}