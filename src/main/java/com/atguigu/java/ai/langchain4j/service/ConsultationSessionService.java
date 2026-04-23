package com.atguigu.java.ai.langchain4j.service;

import com.atguigu.java.ai.langchain4j.entity.ConsultationSession;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 问诊会话业务层
 */
public interface ConsultationSessionService extends IService<ConsultationSession> {

    /**
     * 创建问诊会话（后端自动生成memoryId）
     *
     * @param userId 用户ID
     * @param patientId 就诊人ID
     * @param title 会话标题
     * @return 会话信息
     */
    ConsultationSession createSession(Long userId, Long patientId, String title);

    /**
     * 查询用户问诊会话列表（按创建时间倒序）
     *
     * @param userId 用户ID
     * @param patientId 就诊人ID（可为空）
     * @return 会话列表
     */
    java.util.List<ConsultationSession> listByUserId(Long userId, Long patientId);

    /**
     * 删除问诊会话，并清理对应聊天记忆
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 是否删除成功
     */
    boolean deleteSession(Long userId, Long sessionId);
}