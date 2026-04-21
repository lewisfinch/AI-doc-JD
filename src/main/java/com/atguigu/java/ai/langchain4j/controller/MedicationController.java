package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.assistant.MedicationAgent;
import com.atguigu.java.ai.langchain4j.bean.ApiResponse;
import com.atguigu.java.ai.langchain4j.bean.MedicationConsultRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用药咨询接口
 */
@Tag(name = "用药咨询")
@RestController
@RequestMapping("/api/medication")
public class MedicationController {

    private final MedicationAgent medicationAgent;

    public MedicationController(MedicationAgent medicationAgent) {
        this.medicationAgent = medicationAgent;
    }

    @Operation(summary = "药物咨询")
    @PostMapping("/consult")
    public ApiResponse<String> consult(@RequestBody MedicationConsultRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return ApiResponse.failure("咨询内容不能为空");
        }
        try {
            return ApiResponse.success(medicationAgent.consult(request.getMessage()));
        } catch (Exception e) {
            return ApiResponse.failure("用药咨询失败：" + e.getMessage());
        }
    }
}