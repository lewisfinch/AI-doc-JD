package com.atguigu.java.ai.langchain4j.service.impl;

import com.atguigu.java.ai.langchain4j.assistant.ReportAnalysisAgent;
import com.atguigu.java.ai.langchain4j.bean.ReportUploadAnalyzeResponse;
import com.atguigu.java.ai.langchain4j.entity.HealthReport;
import com.atguigu.java.ai.langchain4j.service.HealthReportService;
import com.atguigu.java.ai.langchain4j.service.ReportAnalysisService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

/**
 * 医学报告上传与解析业务实现
 */
@Service
public class ReportAnalysisServiceImpl implements ReportAnalysisService {

    private final ReportAnalysisAgent reportAnalysisAgent;
    private final HealthReportService healthReportService;

    @Value("${report.upload-dir:uploads/reports}")
    private String uploadDir;

    public ReportAnalysisServiceImpl(ReportAnalysisAgent reportAnalysisAgent, HealthReportService healthReportService) {
        this.reportAnalysisAgent = reportAnalysisAgent;
        this.healthReportService = healthReportService;
    }

    @Override
    public ReportUploadAnalyzeResponse uploadAndAnalyze(Long userId, Long patientId, Integer reportType, MultipartFile file) {
        validateRequest(userId, patientId, reportType, file);

        String originName = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        String extension = getExtension(originName).toLowerCase(Locale.ROOT);
        String savedName = UUID.randomUUID().toString().replace("-", "") + (extension.isBlank() ? "" : "." + extension);

        Path uploadPath;
        try {
            Path baseDir = Path.of(uploadDir);
            Files.createDirectories(baseDir);
            uploadPath = baseDir.resolve(savedName);
            file.transferTo(uploadPath);
        } catch (Exception e) {
            throw new IllegalStateException("报告文件保存失败", e);
        }

        String extractedText = extractText(uploadPath, extension, file);
        if (extractedText == null || extractedText.isBlank()) {
            throw new IllegalArgumentException("报告文本解析失败，无法进行AI解读");
        }

        String prompt = "以下是医学检测报告原文，请按约定结构解读：\n" + extractedText;
        String analysisResult = reportAnalysisAgent.analyze(prompt);

        HealthReport healthReport = new HealthReport();
        healthReport.setUserId(userId);
        healthReport.setPatientId(patientId);
        healthReport.setFileName(originName);
        healthReport.setFileUrl(uploadPath.toString());
        healthReport.setReportType(reportType);
        healthReport.setAnalysisResult(analysisResult);
        healthReport.setCreateTime(LocalDateTime.now());
        healthReportService.saveAnalyzedReport(healthReport);

        return new ReportUploadAnalyzeResponse()
            .setReportId(healthReport.getId())
            .setFileName(originName)
            .setFileUrl(uploadPath.toString())
            .setReportType(reportType)
            .setAnalysisResult(analysisResult)
            .setExtractedText(extractedText);
    }

    private void validateRequest(Long userId, Long patientId, Integer reportType, MultipartFile file) {
        if (userId == null || patientId == null || reportType == null) {
            throw new IllegalArgumentException("userId、patientId、reportType不能为空");
        }
        if (reportType < 1 || reportType > 3) {
            throw new IllegalArgumentException("reportType非法，必须是1(体检)、2(化验)、3(影像)");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
    }

    private String extractText(Path uploadPath, String extension, MultipartFile file) {
        try {
            if ("pdf".equals(extension)) {
                Document document = FileSystemDocumentLoader.loadDocument(uploadPath, new ApachePdfBoxDocumentParser());
                return document.text();
            }

            if ("txt".equals(extension) || "md".equals(extension) || "text".equals(extension)) {
                Document document = FileSystemDocumentLoader.loadDocument(uploadPath, new TextDocumentParser());
                return document.text();
            }

            if ("png".equals(extension) || "jpg".equals(extension) || "jpeg".equals(extension) || "bmp".equals(extension)) {
                // 这里假设项目已完成OCR接入；当前示例以占位文本演示链路闭环
                return "OCR_RESULT(" + file.getOriginalFilename() + "): 请替换为实际OCR服务识别结果。";
            }

            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("报告文本解析失败", e);
        }
    }

    private String getExtension(String fileName) {
        int idx = fileName.lastIndexOf(".");
        if (idx < 0 || idx == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(idx + 1);
    }
}