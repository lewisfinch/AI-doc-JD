package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.bean.ApiResponse;
import com.atguigu.java.ai.langchain4j.entity.Patient;
import com.atguigu.java.ai.langchain4j.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @GetMapping
    public ApiResponse<List<Patient>> listByUserId(@RequestParam("userId") Long userId) {
        if (userId == null) {
            return ApiResponse.failure("userId不能为空");
        }
        return ApiResponse.success(patientService.listByUserId(userId));
    }

    @Operation(summary = "按用户ID获取就诊人列表（兼容旧路径）")
    @GetMapping("/list")
    public ApiResponse<List<Patient>> listByUserIdCompat(@RequestParam("userId") Long userId) {
        return listByUserId(userId);
    }

    @Operation(summary = "创建就诊人")
    @PostMapping
    public ApiResponse<Patient> create(@RequestBody Patient patient) {
        String validateResult = validatePatientPayload(patient);
        if (validateResult != null) {
            return ApiResponse.failure(validateResult);
        }
        patient.setId(null);
        boolean saved = patientService.save(patient);
        if (!saved) {
            return ApiResponse.failure("创建就诊人失败");
        }
        return ApiResponse.success(patient);
    }

    @Operation(summary = "获取就诊人详情")
    @GetMapping("/{id}")
    public ApiResponse<Patient> detail(@PathVariable("id") Long id, @RequestParam("userId") Long userId) {
        if (id == null || userId == null) {
            return ApiResponse.failure("id、userId不能为空");
        }
        Patient existing = patientService.getById(id);
        if (existing == null || !userId.equals(existing.getUserId())) {
            return ApiResponse.failure("就诊人不存在");
        }
        return ApiResponse.success(existing);
    }

    @Operation(summary = "更新就诊人")
    @PutMapping("/{id}")
    public ApiResponse<Patient> update(@PathVariable("id") Long id, @RequestBody Patient patient) {
        if (id == null) {
            return ApiResponse.failure("id不能为空");
        }
        String validateResult = validatePatientPayload(patient);
        if (validateResult != null) {
            return ApiResponse.failure(validateResult);
        }

        Patient existing = patientService.getById(id);
        if (existing == null) {
            return ApiResponse.failure("就诊人不存在");
        }
        if (!existing.getUserId().equals(patient.getUserId())) {
            return ApiResponse.failure("无权修改该就诊人");
        }

        existing.setName(patient.getName())
            .setGender(patient.getGender())
            .setAge(patient.getAge())
            .setRelation(patient.getRelation())
            .setMedicalHistory(patient.getMedicalHistory());
        boolean updated = patientService.updateById(existing);
        if (!updated) {
            return ApiResponse.failure("更新就诊人失败");
        }
        return ApiResponse.success(existing);
    }

    @Operation(summary = "删除就诊人")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Long id, @RequestParam("userId") Long userId) {
        if (id == null || userId == null) {
            return ApiResponse.failure("id、userId不能为空");
        }
        Patient existing = patientService.getById(id);
        if (existing == null || !userId.equals(existing.getUserId())) {
            return ApiResponse.failure("就诊人不存在");
        }
        boolean removed = patientService.removeById(id);
        if (!removed) {
            return ApiResponse.failure("删除就诊人失败");
        }
        return ApiResponse.success(true);
    }

    private String validatePatientPayload(Patient patient) {
        if (patient == null) {
            return "请求体不能为空";
        }
        if (patient.getUserId() == null) {
            return "userId不能为空";
        }
        if (patient.getName() == null || patient.getName().isBlank()) {
            return "姓名不能为空";
        }
        if (patient.getGender() == null || patient.getGender().isBlank()) {
            return "性别不能为空";
        }
        if (patient.getAge() == null || patient.getAge() <= 0 || patient.getAge() > 150) {
            return "年龄非法";
        }
        if (patient.getRelation() == null || patient.getRelation().isBlank()) {
            return "关系不能为空";
        }
        return null;
    }
}