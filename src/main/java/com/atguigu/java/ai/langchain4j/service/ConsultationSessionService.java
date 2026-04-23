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
     * 查询指定就诊人的问诊会话列表（按创建时间倒序）
     *
     * @param patientId 就诊人ID
     * @return 会话列表
     */
    java.util.List<ConsultationSession> listByPatientId(Long patientId);

    /**
     * 删除问诊会话，并清理对应聊天记忆
     *
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 是否删除成功
     */
    boolean deleteSession(Long userId, Long sessionId);

    /**
     * 按就诊人删除全部问诊会话，并清理对应聊天记忆
     *
     * @param patientId 就诊人ID
     * @return 删除条数
     */
    int deleteByPatientId(Long patientId);

    /**
     * 判断是否为该会话首轮问答（用户第一次提问前）
     *
     * @param memoryId 会话memoryId
     * @return 是否首轮
     */
    boolean isFirstRound(String memoryId);

    /**
     * 在首轮问答完成后更新会话标题（20字内）
     *
     * @param memoryId 会话memoryId
     * @param userQuestion 用户首问
     * @param aiAnswer 首轮AI回答
     */
    void updateTitleAfterFirstRound(String memoryId, String userQuestion, String aiAnswer);
}