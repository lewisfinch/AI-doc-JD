package com.atguigu.java.ai.langchain4j.service.impl;

import com.atguigu.java.ai.langchain4j.entity.ConsultationSession;
import com.atguigu.java.ai.langchain4j.mapper.ConsultationSessionMapper;
import com.atguigu.java.ai.langchain4j.service.ConsultationSessionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 问诊会话业务实现
 */
@Service
public class ConsultationSessionServiceImpl extends ServiceImpl<ConsultationSessionMapper, ConsultationSession>
    implements ConsultationSessionService {

    @Override
    public ConsultationSession createSession(Long userId, Long patientId, String title) {
        if (userId == null || patientId == null) {
            throw new IllegalArgumentException("userId和patientId不能为空");
        }
        ConsultationSession session = new ConsultationSession();
        session.setUserId(userId);
        session.setPatientId(patientId);
        session.setMemoryId(UUID.randomUUID().toString());
        session.setTitle((title == null || title.isBlank()) ? "新建问诊" : title);
        session.setStatus(0);
        session.setCreateTime(LocalDateTime.now());
        baseMapper.insert(session);
        return session;
    }

    @Override
    public List<ConsultationSession> listByUserId(Long userId, Long patientId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId不能为空");
        }
        LambdaQueryWrapper<ConsultationSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationSession::getUserId, userId)
            .orderByDesc(ConsultationSession::getCreateTime);
        if (patientId != null) {
            wrapper.eq(ConsultationSession::getPatientId, patientId);
        }
        return baseMapper.selectList(wrapper);
    }
}