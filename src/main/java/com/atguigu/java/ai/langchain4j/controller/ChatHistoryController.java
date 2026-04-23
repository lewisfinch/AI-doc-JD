package com.atguigu.java.ai.langchain4j.controller;

import com.atguigu.java.ai.langchain4j.store.MongoChatMemoryStore;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天记录历史接口
 */
@Tag(name = "聊天记录历史")
@RestController
@RequestMapping("/api/chat")
public class ChatHistoryController {

    private final MongoChatMemoryStore mongoChatMemoryStore;

    public ChatHistoryController(MongoChatMemoryStore mongoChatMemoryStore) {
        this.mongoChatMemoryStore = mongoChatMemoryStore;
    }

    @Operation(summary = "获取指定会话的历史聊天记录")
    @GetMapping("/history/{memoryId}")
    public Map<String, Object> getChatHistory(@PathVariable("memoryId") String memoryId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<ChatMessage> messages = mongoChatMemoryStore.getMessages(memoryId);

            List<Map<String, String>> formattedMessages = messages.stream()
                .filter(msg -> !"SYSTEM".equals(msg.type().name()))
                .map(msg -> {
                    Map<String, String> messageMap = new HashMap<>();
                    messageMap.put("role", msg.type().name().toLowerCase());

                    String content = "";
                    if (msg instanceof UserMessage) {
                        UserMessage userMessage = (UserMessage) msg;
                        if (userMessage.hasSingleText()) {
                            content = userMessage.singleText();
                        } else if (!userMessage.contents().isEmpty() && userMessage.contents().get(0) instanceof TextContent) {
                            content = ((TextContent) userMessage.contents().get(0)).text();
                        }
                    } else if (msg instanceof AiMessage) {
                        content = ((AiMessage) msg).text();
                    }

                    messageMap.put("content", content);
                    return messageMap;
                })
                .collect(Collectors.toList());

            result.put("code", 0);
            result.put("message", "success");
            result.put("data", formattedMessages);

        } catch (Exception e) {
            result.put("code", 1);
            result.put("message", "获取聊天记录失败: " + e.getMessage());
            result.put("data", null);
        }

        return result;
    }
}