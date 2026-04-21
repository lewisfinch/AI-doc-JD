package com.atguigu.java.ai.langchain4j.service;

import com.atguigu.java.ai.langchain4j.entity.HealthReport;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 健康报告业务层
 */
public interface HealthReportService extends IService<HealthReport> {

    /**
     * 保存模型解析后的健康报告
     *
     * @param healthReport 报告实体
     * @return 入库后的实体
     */
    HealthReport saveAnalyzedReport(HealthReport healthReport);

    /**
     * 根据用户和就诊人查询报告列表（按创建时间倒序）
     *
     * @param userId 用户ID
     * @param patientId 就诊人ID（可为空）
     * @return 报告列表
     */
    java.util.List<HealthReport> listByUserIdAndPatientId(Long userId, Long patientId);
}