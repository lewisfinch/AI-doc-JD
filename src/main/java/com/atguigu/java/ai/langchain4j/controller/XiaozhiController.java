package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.assistant.XiaozhiAgent;
import com.atguigu.java.ai.langchain4j.bean.ChatForm;
import com.atguigu.java.ai.langchain4j.service.ConsultationSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "硅谷小智")
@RestController
@RequestMapping("/api/xiaozhi")
public class XiaozhiController {

    private final XiaozhiAgent xiaozhiAgent;
    private final ConsultationSessionService consultationSessionService;

    public XiaozhiController(XiaozhiAgent xiaozhiAgent,
                             ConsultationSessionService consultationSessionService) {
        this.xiaozhiAgent = xiaozhiAgent;
        this.consultationSessionService = consultationSessionService;
    }

    @Operation(summary = "对话")
    @PostMapping(value = "/chat", produces = "text/stream;charset=utf-8")
    public Flux<String> chat(@RequestBody ChatForm chatForm) {
        boolean firstRound = consultationSessionService.isFirstRound(chatForm.getMemoryId());
        Flux<String> stream = xiaozhiAgent.chat(chatForm.getMemoryId(), chatForm.getMessage());
        if (!firstRound) {
            return stream;
        }

        StringBuilder fullAnswer = new StringBuilder();
        return stream.doOnNext(fullAnswer::append)
            .doOnComplete(() -> consultationSessionService.updateTitleAfterFirstRound(
                chatForm.getMemoryId(),
                chatForm.getMessage(),
                fullAnswer.toString()
            ));
    }
}