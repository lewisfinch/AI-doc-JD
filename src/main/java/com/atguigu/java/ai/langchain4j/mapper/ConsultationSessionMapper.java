package com.atguigu.java.ai.langchain4j.mapper;

import com.atguigu.java.ai.langchain4j.entity.ConsultationSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问诊会话数据访问层
 */
@Mapper
public interface ConsultationSessionMapper extends BaseMapper<ConsultationSession> {
}