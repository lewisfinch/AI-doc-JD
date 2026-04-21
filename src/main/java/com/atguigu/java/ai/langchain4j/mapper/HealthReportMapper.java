package com.atguigu.java.ai.langchain4j.mapper;

import com.atguigu.java.ai.langchain4j.entity.HealthReport;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健康报告数据访问层
 */
@Mapper
public interface HealthReportMapper extends BaseMapper<HealthReport> {
}