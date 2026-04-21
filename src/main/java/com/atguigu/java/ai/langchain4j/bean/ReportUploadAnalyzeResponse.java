package com.atguigu.java.ai.langchain4j.bean;

/**
 * 报告上传并解析响应
 */
public class ReportUploadAnalyzeResponse {

    private Long reportId;
    private String fileName;
    private String fileUrl;
    private Integer reportType;
    private String analysisResult;
    private String extractedText;

    public Long getReportId() {
        return reportId;
    }

    public ReportUploadAnalyzeResponse setReportId(Long reportId) {
        this.reportId = reportId;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public ReportUploadAnalyzeResponse setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public ReportUploadAnalyzeResponse setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
        return this;
    }

    public Integer getReportType() {
        return reportType;
    }

    public ReportUploadAnalyzeResponse setReportType(Integer reportType) {
        this.reportType = reportType;
        return this;
    }

    public String getAnalysisResult() {
        return analysisResult;
    }

    public ReportUploadAnalyzeResponse setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
        return this;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public ReportUploadAnalyzeResponse setExtractedText(String extractedText) {
        this.extractedText = extractedText;
        return this;
    }
}