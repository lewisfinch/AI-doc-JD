import React, { useState, useRef, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

function Chat() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [messages, setMessages] = useState([
        { id: 1, role: 'ai', content: '您好，我是京东健康AI医生。请问有什么可以帮您？您可详细描述您的症状、持续时间及伴随情况。', isTyping: false }
    ]);
    const [input, setInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [isLoadingHistory, setIsLoadingHistory] = useState(false);
    const messagesEndRef = useRef(null);

    // 从URL参数获取memoryId
    const [memoryId, setMemoryId] = useState(() => {
        const urlMemoryId = searchParams.get('memoryId');
        return urlMemoryId || null;
    });

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };
    
    // 加载历史聊天记录
    const loadChatHistory = async (currentMemoryId) => {
        if (!currentMemoryId) return;
        
        setIsLoadingHistory(true);
        try {
            const response = await fetch(`/api/chat/history/${currentMemoryId}`);
            if (response.ok) {
                const data = await response.json();
                if (data.code === 0 && data.data) {
                    const historyMessages = data.data.map((msg, index) => ({
                        id: index + 1,
                        role: msg.role,
                        content: msg.content,
                        isTyping: false
                    }));
                    
                    if (historyMessages.length > 0) {
                        setMessages(historyMessages);
                    }
                }
            }
        } catch (error) {
            console.error('加载历史聊天记录失败:', error);
        } finally {
            setIsLoadingHistory(false);
        }
    };
    
    useEffect(() => {
        scrollToBottom();
    }, [messages]);
    
    // 当memoryId变化时加载历史记录
    useEffect(() => {
        if (memoryId) {
            loadChatHistory(memoryId);
        }
    }, [memoryId]);


    const handleSend = async () => {
        if (!input.trim() || isLoading) return;

        const userMessage = input.trim();
        const newUserMsg = { id: Date.now(), role: 'user', content: userMessage };

        const aiMsgId = Date.now() + 1;
        setMessages(prev => [...prev, newUserMsg, { id: aiMsgId, role: 'ai', content: '', isTyping: true }]);

        setInput('');
        setIsLoading(true);

        // ================= 新增的核心业务逻辑 =================
        let activeMemoryId = memoryId;

        // 如果是第一次发消息，说明没有 memoryId，先去 MySQL 建个档！
        if (!activeMemoryId) {
            try {
                const sessionRes = await fetch('/api/consultation/create', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        userId: 1,      // 写死 1，模拟当前登录用户的 ID
                        patientId: 1,   // 写死 1，模拟当前选中的就诊人 ID
                        title: userMessage.slice(0, 15) // 截取用户第一句话的前15个字作为 MySQL 里的会话标题
                    })
                });
                const sessionData = await sessionRes.json();

                // 假设后端返回的数据结构是 { code: 200, data: { memoryId: "xxxx" } }
                if (sessionData.data && sessionData.data.memoryId) {
                    activeMemoryId = sessionData.data.memoryId;
                    setMemoryId(activeMemoryId); // 保存下来，这通对话以后都用这个 ID
                    console.log("MySQL 会话创建成功，memoryId:", activeMemoryId);
                }
            } catch (err) {
                console.error("向 MySQL 注册会话失败，降级使用本地 ID", err);
                activeMemoryId = Math.floor(Math.random() * 1000000).toString();
            }
        }
        // ===================================================

        try {
            // 3. 带着刚刚从 MySQL 拿到的真实 activeMemoryId 去请求大模型
            const response = await fetch('/api/xiaozhi/chat', {
                method: 'POST',
                headers: {
                    'Accept': 'text/stream',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    memoryId: activeMemoryId, // 使用真实的 ID
                    message: userMessage
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const reader = response.body.getReader();
            const decoder = new TextDecoder('utf-8');
            let done = false;
            let fullContent = '';

            while (!done) {
                const { value, done: readerDone } = await reader.read();
                done = readerDone;

                if (value) {
                    const chunk = decoder.decode(value, { stream: true });
                    // 如果后端返回包含 data: 前缀，可以使用 chunk.replace(/data: /g, '') 处理
                    fullContent += chunk;

                    setMessages(prev => prev.map(msg =>
                        msg.id === aiMsgId
                            ? { ...msg, content: fullContent }
                            : msg
                    ));
                }
            }

            setMessages(prev => prev.map(msg =>
                msg.id === aiMsgId ? { ...msg, isTyping: false } : msg
            ));

        } catch (error) {
            console.error('API Error:', error);
            setMessages(prev => prev.map(msg =>
                msg.id === aiMsgId
                    ? { ...msg, content: '网络有点开小差，请稍后再试哦。', isTyping: false }
                    : msg
            ));
        } finally {
            setIsLoading(false);
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') handleSend();
    };

    return (
        <div className="flex flex-col h-screen bg-gray-50">
            {/* 头部 */}
            <div className="flex-none bg-white shadow-sm px-4 py-3 flex items-center justify-between sticky top-0 z-10">
                <button onClick={() => navigate(-1)} className="text-gray-600 p-2 hover:bg-gray-100 rounded-full transition-colors">
                    <i className="fas fa-chevron-left text-lg"></i>
                </button>
                <div className="flex items-center space-x-2">
                    <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center">
                        <i className="fas fa-user-md text-blue-600"></i>
                    </div>
                    <div>
                        <h1 className="text-lg font-medium text-gray-800">全科问诊医生</h1>
                        <p className="text-xs text-green-500">在线</p>
                    </div>
                </div>
                <button className="text-gray-600 p-2 hover:bg-gray-100 rounded-full transition-colors">
                    <i className="fas fa-ellipsis-h"></i>
                </button>
            </div>

            {/* 加载历史记录提示 */}
            {isLoadingHistory && (
                <div className="flex-none bg-blue-50 px-4 py-2 text-center text-sm text-blue-600">
                    <i className="fas fa-spinner fa-spin mr-2"></i>
                    正在加载历史聊天记录...
                </div>
            )}

            {/* 聊天区域 */}
            <div className="flex-1 overflow-y-auto p-4 space-y-4">
                {messages.map((msg) => (
                    <div key={msg.id} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                        {msg.role === 'ai' && (
                            <div className="w-8 h-8 rounded-full bg-blue-100 flex-shrink-0 flex items-center justify-center mr-2 mt-1">
                                <i className="fas fa-robot text-blue-600 text-sm"></i>
                            </div>
                        )}

                        <div className={`max-w-[75%] rounded-2xl px-4 py-3 shadow-sm ${
                            msg.role === 'user'
                                ? 'bg-blue-600 text-white rounded-tr-sm'
                                : 'bg-white text-gray-800 rounded-tl-sm border border-gray-100'
                        }`}>
                            <p className="text-[15px] leading-relaxed whitespace-pre-wrap">{msg.content}</p>
                            {msg.isTyping && (
                                <span className="inline-block w-1.5 h-4 bg-blue-400 ml-1 animate-pulse align-middle"></span>
                            )}
                        </div>
                    </div>
                ))}
                <div ref={messagesEndRef} />
            </div>

            {/* 底部输入框 */}
            <div className="flex-none bg-white p-3 border-t border-gray-200 safe-area-bottom">
                <div className="flex items-end space-x-2">
                    <div className="flex-1 bg-gray-100 rounded-2xl px-4 py-2 min-h-[44px] flex items-center">
                        <input
                            type="text"
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            onKeyPress={handleKeyPress}
                            placeholder="描述您的症状..."
                            className="w-full bg-transparent outline-none text-[15px] text-gray-800 placeholder-gray-400"
                            disabled={isLoading}
                        />
                    </div>
                    <button
                        onClick={handleSend}
                        disabled={!input.trim() || isLoading}
                        className={`p-2 rounded-full w-11 h-11 flex items-center justify-center transition-all shadow-sm ${
                            input.trim() && !isLoading
                                ? 'bg-blue-600 text-white hover:bg-blue-700'
                                : 'bg-gray-200 text-gray-400'
                        }`}
                    >
                        {isLoading ? <i className="fas fa-spinner fa-spin"></i> : <i className="fas fa-paper-plane"></i>}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default Chat;