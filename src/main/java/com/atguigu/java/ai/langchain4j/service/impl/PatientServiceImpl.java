package com.atguigu.java.ai.langchain4j.service.impl;

import com.atguigu.java.ai.langchain4j.entity.Patient;
import com.atguigu.java.ai.langchain4j.mapper.PatientMapper;
import com.atguigu.java.ai.langchain4j.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 就诊人业务实现
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Override
    public List<Patient> listByUserId(Long userId) {
        LambdaQueryWrapper<Patient> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Patient::getUserId, userId)
            .orderByDesc(Patient::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }
}