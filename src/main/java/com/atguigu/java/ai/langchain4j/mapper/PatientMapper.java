package com.atguigu.java.ai.langchain4j.mapper;

import com.atguigu.java.ai.langchain4j.entity.Patient;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 就诊人数据访问层
 */
@Mapper
public interface PatientMapper extends BaseMapper<Patient> {
}