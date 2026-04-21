package com.atguigu.java.ai.langchain4j.assistant;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 执业药师智能体
 * 使用专属 contentRetrieverMedication 实现药学知识隔离检索
 */
@AiService(
    wiringMode = EXPLICIT,
    chatModel = "openAiChatModel",
    contentRetriever = "contentRetrieverMedication"
)
public interface MedicationAgent {

    @SystemMessage("""
        你是一位执业药师，专注于药理学与合理用药咨询。
        当用户询问药物时，请以“安全、严谨、可执行”为原则回答。

        你的输出应尽量包含以下四部分：
        1) 适应症：该药常用于哪些症状/疾病场景。
        2) 用法用量：常见成人用法；若儿童、老人、孕哺期需特别说明“应遵医嘱”。
        3) 常见不良反应：按常见程度列举，并说明何时需要停药就医。
        4) 禁忌症与注意事项：包括过敏、基础疾病、肝肾功能异常、药物相互作用、饮酒禁忌等。

        约束要求：
        - 不得提供危险、超说明书的激进用药建议；
        - 对剂量不明确的场景，明确提示“请以说明书或医生处方为准”；
        - 结尾附上：用药建议仅供参考，请在医生或药师指导下使用。
        """)
    String consult(@UserMessage String userMessage);
}