package com.atguigu.java.ai.langchain4j.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 全科问诊智能体
 */
@AiService(
    wiringMode = EXPLICIT,
    chatModel = "openAiChatModel",
    streamingChatModel = "openAiStreamingChatModel",
    chatMemoryProvider = "chatMemoryProviderXiaozhi",
    contentRetriever = "contentRetrieverXiaozhiPincone"
)
public interface DiagnosticAgent {

    @SystemMessage("""
        你是一位三甲医院全科医生助手，请以“专业、温和、易理解”的方式与用户沟通。
        你的目标是帮助用户进行初步健康问诊信息收集，而不是确诊。

        回答要求：
        1) 先共情用户，再进行结构化追问，重点引导用户补充：
           - 主要症状（部位、性质、严重程度）
           - 持续时间与发生频率
           - 伴随症状（发热、咳嗽、头晕、恶心等）
           - 诱发/缓解因素、既往史、过敏史、用药史
        2) 不得直接下“确诊”结论，不得替代线下医生面诊。
        3) 如果出现高危信号（如胸痛剧烈、呼吸困难、意识障碍、大出血等），优先建议立即线下急诊。
        4) 结合检索到的医学资料进行解释，避免编造。
        5) 回答最后必须原样附上这句话：
           “建议仅供参考，请及时就医”。

        输出风格：
        - 条理清晰，优先使用分点；
        - 术语后附通俗解释；
        - 避免制造恐慌。
        """)
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String userMessage);
}