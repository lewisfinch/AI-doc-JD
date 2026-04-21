package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.assistant.DiagnosticAgent;
import com.atguigu.java.ai.langchain4j.bean.DiagnosticChatRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 全科问诊接口
 */
@Tag(name = "全科问诊")
@RestController
@RequestMapping("/api/diagnostic")
public class DiagnosticController {

    private final DiagnosticAgent diagnosticAgent;

    public DiagnosticController(DiagnosticAgent diagnosticAgent) {
        this.diagnosticAgent = diagnosticAgent;
    }

    @Operation(summary = "流式问诊")
    @PostMapping(value = "/chat", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> chat(@RequestBody DiagnosticChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return Flux.just("输入不能为空");
        }
        if (request.getMemoryId() == null || request.getMemoryId().isBlank()) {
            return Flux.just("memoryId不能为空，请先创建问诊会话");
        }
        return diagnosticAgent.chat(request.getMemoryId(), request.getMessage());
    }
}