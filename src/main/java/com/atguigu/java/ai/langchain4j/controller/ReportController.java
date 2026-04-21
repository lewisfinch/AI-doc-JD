package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.bean.ApiResponse;
import com.atguigu.java.ai.langchain4j.bean.ReportUploadAnalyzeResponse;
import com.atguigu.java.ai.langchain4j.service.ReportAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 医学报告接口
 */
@Tag(name = "医学报告")
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportAnalysisService reportAnalysisService;

    public ReportController(ReportAnalysisService reportAnalysisService) {
        this.reportAnalysisService = reportAnalysisService;
    }

    @Operation(summary = "上传并实时解析医学报告")
    @PostMapping("/uploadAndAnalyze")
    public ApiResponse<ReportUploadAnalyzeResponse> uploadAndAnalyze(@RequestParam("userId") Long userId,
                                                                     @RequestParam("patientId") Long patientId,
                                                                     @RequestParam("reportType") Integer reportType,
                                                                     @RequestParam("file") MultipartFile file) {
        try {
            ReportUploadAnalyzeResponse response = reportAnalysisService.uploadAndAnalyze(userId, patientId, reportType, file);
            return ApiResponse.success(response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.failure(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.failure("报告上传解析失败：" + e.getMessage());
        }
    }
}