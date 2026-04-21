package com.atguigu.java.ai.langchain4j.service.impl;

import com.atguigu.java.ai.langchain4j.entity.HealthReport;
import com.atguigu.java.ai.langchain4j.mapper.HealthReportMapper;
import com.atguigu.java.ai.langchain4j.service.HealthReportService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 健康报告业务实现
 */
@Service
public class HealthReportServiceImpl extends ServiceImpl<HealthReportMapper, HealthReport> implements HealthReportService {

    @Override
    public HealthReport saveAnalyzedReport(HealthReport healthReport) {
        if (healthReport == null) {
            throw new IllegalArgumentException("healthReport不能为空");
        }
        if (healthReport.getUserId() == null || healthReport.getPatientId() == null) {
            throw new IllegalArgumentException("报告归属信息不完整");
        }
        if (healthReport.getCreateTime() == null) {
            healthReport.setCreateTime(LocalDateTime.now());
        }
        baseMapper.insert(healthReport);
        return healthReport;
    }

    @Override
    public List<HealthReport> listByUserIdAndPatientId(Long userId, Long patientId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId不能为空");
        }
        LambdaQueryWrapper<HealthReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthReport::getUserId, userId)
            .orderByDesc(HealthReport::getCreateTime);
        if (patientId != null) {
            wrapper.eq(HealthReport::getPatientId, patientId);
        }
        return baseMapper.selectList(wrapper);
    }
}