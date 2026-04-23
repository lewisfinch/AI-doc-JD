package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.bean.ApiResponse;
import com.atguigu.java.ai.langchain4j.bean.ConsultationSessionCreateRequest;
import com.atguigu.java.ai.langchain4j.entity.ConsultationSession;
import com.atguigu.java.ai.langchain4j.service.ConsultationSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 问诊会话接口
 */
@Tag(name = "问诊会话管理")
@RestController
@RequestMapping("/api/consultation")
public class ConsultationSessionController {

    private final ConsultationSessionService consultationSessionService;

    public ConsultationSessionController(ConsultationSessionService consultationSessionService) {
        this.consultationSessionService = consultationSessionService;
    }

    @Operation(summary = "创建问诊会话（后端生成memoryId）")
    @PostMapping("/create")
    public ApiResponse<ConsultationSession> create(@RequestBody ConsultationSessionCreateRequest request) {
        if (request == null || request.getUserId() == null || request.getPatientId() == null) {
            return ApiResponse.failure("userId、patientId不能为空");
        }

        try {
            ConsultationSession session = consultationSessionService.createSession(
                request.getUserId(),
                request.getPatientId(),
                request.getTitle()
            );
            return ApiResponse.success(session);
        } catch (Exception e) {
            return ApiResponse.failure("创建会话失败：" + e.getMessage());
        }
    }

    @Operation(summary = "查询问诊会话列表")
    @GetMapping
    public ApiResponse<List<ConsultationSession>> list(@RequestParam("patientId") Long patientId) {
        if (patientId == null) {
            return ApiResponse.failure("patientId不能为空");
        }
        try {
            return ApiResponse.success(consultationSessionService.listByPatientId(patientId));
        } catch (Exception e) {
            return ApiResponse.failure("查询会话失败：" + e.getMessage());
        }
    }

    @Operation(summary = "查询问诊会话列表（兼容旧路径）")
    @GetMapping("/list")
    public ApiResponse<List<ConsultationSession>> listCompat(@RequestParam("patientId") Long patientId) {
        return list(patientId);
    }

    @Operation(summary = "删除问诊会话")
    @DeleteMapping("/{sessionId}")
    public ApiResponse<Boolean> delete(@PathVariable("sessionId") Long sessionId,
                                       @RequestParam("userId") Long userId) {
        if (sessionId == null || userId == null) {
            return ApiResponse.failure("sessionId、userId不能为空");
        }
        try {
            boolean deleted = consultationSessionService.deleteSession(userId, sessionId);
            if (!deleted) {
                return ApiResponse.failure("会话不存在或无权限删除");
            }
            return ApiResponse.success(true);
        } catch (Exception e) {
            return ApiResponse.failure("删除会话失败：" + e.getMessage());
        }
    }
}