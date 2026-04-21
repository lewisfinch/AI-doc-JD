package com.atguigu.java.ai.langchain4j.service;

import com.atguigu.java.ai.langchain4j.entity.Patient;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 就诊人业务层
 */
public interface PatientService extends IService<Patient> {

    /**
     * 根据用户ID查询就诊人列表（按创建时间倒序）
     *
     * @param userId 用户ID
     * @return 就诊人列表
     */
    java.util.List<Patient> listByUserId(Long userId);
}