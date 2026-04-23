package com.atguigu.java.ai.langchain4j.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 问诊会话标题总结智能体
 */
@AiService(
    wiringMode = EXPLICIT,
    chatModel = "openAiChatModel"
)
public interface ConsultationTitleAgent {

    @SystemMessage("""
        你是医疗问诊标题生成助手。
        请根据“用户首轮提问”和“AI首轮回答”生成一个问诊记录标题。

        输出要求：
        1) 必须是中文；
        2) 不超过20个汉字；
        3) 直接输出标题本身，不要加引号、序号、标点解释；
        4) 语义准确，能概括本次问诊核心问题；
        5) 避免使用“问诊记录”“咨询”等空泛词。
        """)
    String summarize(@UserMessage String content);
}