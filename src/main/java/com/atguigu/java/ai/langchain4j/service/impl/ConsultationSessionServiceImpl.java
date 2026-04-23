package com.atguigu.java.ai.langchain4j.service.impl;

import com.atguigu.java.ai.langchain4j.assistant.ConsultationTitleAgent;
import com.atguigu.java.ai.langchain4j.entity.ConsultationSession;
import com.atguigu.java.ai.langchain4j.mapper.ConsultationSessionMapper;
import com.atguigu.java.ai.langchain4j.service.ConsultationSessionService;
import com.atguigu.java.ai.langchain4j.store.MongoChatMemoryStore;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 问诊会话业务实现
 */
@Service
public class ConsultationSessionServiceImpl extends ServiceImpl<ConsultationSessionMapper, ConsultationSession>
    implements ConsultationSessionService {

    private static final String DEFAULT_GREETING = "您好，我是京东健康AI医生。请问有什么可以帮您？您可详细描述您的症状、持续时间及伴随情况。";

    private final MongoChatMemoryStore mongoChatMemoryStore;
    private final ConsultationTitleAgent consultationTitleAgent;

    public ConsultationSessionServiceImpl(MongoChatMemoryStore mongoChatMemoryStore,
                                          ConsultationTitleAgent consultationTitleAgent) {
        this.mongoChatMemoryStore = mongoChatMemoryStore;
        this.consultationTitleAgent = consultationTitleAgent;
    }

    @Override
    public ConsultationSession createSession(Long userId, Long patientId, String title) {
        if (userId == null || patientId == null) {
            throw new IllegalArgumentException("userId和patientId不能为空");
        }
        ConsultationSession session = new ConsultationSession();
        session.setUserId(userId);
        session.setPatientId(patientId);
        session.setMemoryId(UUID.randomUUID().toString());
        session.setTitle("新建问诊");
        session.setStatus(0);
        session.setCreateTime(LocalDateTime.now());
        baseMapper.insert(session);

        mongoChatMemoryStore.updateMessages(
            session.getMemoryId(),
            Collections.singletonList(AiMessage.from(DEFAULT_GREETING))
        );

        return session;
    }

    @Override
    public List<ConsultationSession> listByPatientId(Long patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId不能为空");
        }
        LambdaQueryWrapper<ConsultationSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationSession::getPatientId, patientId)
            .orderByDesc(ConsultationSession::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public boolean isFirstRound(String memoryId) {
        if (memoryId == null || memoryId.isBlank()) {
            return false;
        }
        List<ChatMessage> messages = mongoChatMemoryStore.getMessages(memoryId);
        return messages.stream().noneMatch(msg -> msg instanceof UserMessage);
    }

    @Override
    public void updateTitleAfterFirstRound(String memoryId, String userQuestion, String aiAnswer) {
        if (memoryId == null || memoryId.isBlank() || userQuestion == null || userQuestion.isBlank()
            || aiAnswer == null || aiAnswer.isBlank()) {
            return;
        }

        LambdaQueryWrapper<ConsultationSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationSession::getMemoryId, memoryId);
        ConsultationSession session = baseMapper.selectOne(wrapper);
        if (session == null) {
            return;
        }
        if (session.getTitle() != null && !session.getTitle().isBlank() && !"新建问诊".equals(session.getTitle())) {
            return;
        }

        String content = "用户提问：" + userQuestion + "\nAI回答：" + aiAnswer;
        String title;
        try {
            title = consultationTitleAgent.summarize(content);
        } catch (Exception e) {
            title = userQuestion;
        }

        title = sanitizeTitle(title, userQuestion);
        session.setTitle(title);
        baseMapper.updateById(session);
    }

    @Override
    public int deleteByPatientId(Long patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId不能为空");
        }

        LambdaQueryWrapper<ConsultationSession> listWrapper = new LambdaQueryWrapper<>();
        listWrapper.eq(ConsultationSession::getPatientId, patientId);
        List<ConsultationSession> sessions = baseMapper.selectList(listWrapper);

        for (ConsultationSession session : sessions) {
            String memoryId = session.getMemoryId();
            if (memoryId != null && !memoryId.isBlank()) {
                mongoChatMemoryStore.deleteMessages(memoryId);
            }
        }

        LambdaQueryWrapper<ConsultationSession> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ConsultationSession::getPatientId, patientId);
        return baseMapper.delete(deleteWrapper);
    }

    @Override
    public boolean deleteSession(Long userId, Long sessionId) {
        if (userId == null || sessionId == null) {
            throw new IllegalArgumentException("userId和sessionId不能为空");
        }

        LambdaQueryWrapper<ConsultationSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationSession::getId, sessionId)
            .eq(ConsultationSession::getUserId, userId);
        ConsultationSession session = baseMapper.selectOne(wrapper);
        if (session == null) {
            return false;
        }

        String memoryId = session.getMemoryId();
        if (memoryId != null && !memoryId.isBlank()) {
            mongoChatMemoryStore.deleteMessages(memoryId);
        }

        return removeById(sessionId);
    }
    private String sanitizeTitle(String title, String fallback) {
        String candidate = title == null ? "" : title.trim();
        candidate = candidate.replace("\n", " ").replace("\r", " ").trim();
        if (candidate.startsWith("\"") && candidate.endsWith("\"") && candidate.length() > 1) {
            candidate = candidate.substring(1, candidate.length() - 1).trim();
        }
        if (candidate.startsWith("“") && candidate.endsWith("”") && candidate.length() > 1) {
            candidate = candidate.substring(1, candidate.length() - 1).trim();
        }

        if (candidate.isBlank()) {
            candidate = fallback == null ? "新建问诊" : fallback.trim();
        }
        if (candidate.isBlank()) {
            candidate = "新建问诊";
        }
        if (candidate.length() > 20) {
            candidate = candidate.substring(0, 20);
        }
        return candidate;
    }
}