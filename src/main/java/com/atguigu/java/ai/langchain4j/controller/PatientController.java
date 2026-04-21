package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.bean.ApiResponse;
import com.atguigu.java.ai.langchain4j.entity.Patient;
import com.atguigu.java.ai.langchain4j.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 就诊人接口
 */
@Tag(name = "就诊人管理")
@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @Operation(summary = "按用户ID获取就诊人列表")
    @GetMapping("/list")
    public ApiResponse<List<Patient>> listByUserId(@RequestParam("userId") Long userId) {
        if (userId == null) {
            return ApiResponse.failure("userId不能为空");
        }
        return ApiResponse.success(patientService.listByUserId(userId));
    }
}