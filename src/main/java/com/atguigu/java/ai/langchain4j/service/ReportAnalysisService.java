package com.atguigu.java.ai.langchain4j.service;

import com.atguigu.java.ai.langchain4j.bean.ReportUploadAnalyzeResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 医学报告上传与解析服务
 */
public interface ReportAnalysisService {

    /**
     * 上传并解析报告，完成模型解读与数据库落库
     *
     * @param userId    用户ID
     * @param patientId 就诊人ID
     * @param reportType 报告类型：1-体检，2-化验，3-影像
     * @param file      上传文件（PDF或图片）
     * @return 解析结果
     */
    ReportUploadAnalyzeResponse uploadAndAnalyze(Long userId, Long patientId, Integer reportType, MultipartFile file);
}