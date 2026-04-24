import React, { useState, useRef, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
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
    const skipNextHistoryLoadRef = useRef(false);

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

    // 历史记录载入后，对最后一条 AI 消息做一次紧急检测（避免重复触发）
    useEffect(() => {
        if (isLoading) return;
        const lastAi = [...messages].reverse().find(m => m.role === 'ai' && !m.isTyping);
        if (lastAi && !emergencyMap[lastAi.id] && detectEmergency(lastAi.content)) {
            triggerEmergencyCheck(lastAi.id, lastAi.content);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [messages, isLoading]);
    
    // 当memoryId变化时加载历史记录
    useEffect(() => {
        if (!memoryId) return;

        if (skipNextHistoryLoadRef.current) {
            skipNextHistoryLoadRef.current = false;
            return;
        }

        loadChatHistory(memoryId);
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
                const currentPatientId = Number(localStorage.getItem('selectedPatientId')) || null;
                if (!currentPatientId) {
                    throw new Error('请先选择当前就诊人');
                }

                const sessionRes = await fetch('/api/consultation/create', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        userId: 1,      // 写死 1，模拟当前登录用户的 ID
                        patientId: currentPatientId,
                        title: userMessage.slice(0, 15) // 截取用户第一句话的前15个字作为 MySQL 里的会话标题
                    })
                });
                const sessionData = await sessionRes.json();

                // 假设后端返回的数据结构是 { code: 200, data: { memoryId: "xxxx" } }
                if (sessionData.data && sessionData.data.memoryId) {
                    activeMemoryId = sessionData.data.memoryId;
                    skipNextHistoryLoadRef.current = true;
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

            // 大模型回答结束：检测病症是否紧急，紧急则弹红色卡片并推荐附近医院
            triggerEmergencyCheck(aiMsgId, fullContent);

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

    // 从AI回答中提取"涉及药品：a、b、c"一行，返回 { cleanedContent, drugs }
    // 规则：匹配最后一处 "涉及药品：..." 到该行结束；药品按中文顿号 "、" 或英文逗号/中文逗号分割
    const extractDrugs = (content) => {
        if (!content) return { cleanedContent: content, drugs: [] };
        // 允许 ：或: ，允许前面有 **、# 等 markdown 标记
        const regex = /(?:^|\n)[\s*#>\-]*涉及药品[：:]\s*([^\n]+)/;
        const match = content.match(regex);
        if (!match) return { cleanedContent: content, drugs: [] };

        const drugsRaw = match[1].trim()
            // 去掉末尾的句号、markdown 粗体标记等
            .replace(/\*+$/g, '')
            .replace(/[。.\s]+$/g, '');

        const drugs = drugsRaw
            .split(/[、,，]/)
            .map(s => s.replace(/[\*`\s]/g, '').trim())
            .filter(Boolean);

        // 从正文中移除这一行，避免重复展示
        const cleanedContent = content.replace(match[0], '').trimEnd();
        return { cleanedContent, drugs };
    };

    // 构造京东药品搜索跳转链接（使用搜索页，跳转到对应药品的商品列表）
    const buildJdUrl = (drugName) => {
        return `https://search.jd.com/Search?keyword=${encodeURIComponent(drugName)}&enc=utf-8`;
    };

    // ======================= 紧急病症检测 & 附近医院推荐 =======================
    // 紧急症状/关键词库（命中任意一个即视为紧急）
    const EMERGENCY_KEYWORDS = [
        '立即就医', '立刻就医', '尽快就医', '马上就医', '急诊', '拨打120', '120',
        '危及生命', '生命危险', '抢救', '休克', '昏迷', '意识不清', '意识丧失',
        '呼吸困难', '呼吸停止', '窒息', '心脏骤停', '心跳骤停', '心梗', '心肌梗死',
        '中风', '脑卒中', '脑出血', '脑梗', '大出血', '咯血', '呕血', '便血不止',
        '剧烈胸痛', '胸痛剧烈', '持续胸痛', '剧烈腹痛', '剧烈头痛',
        '抽搐', '癫痫持续', '过敏性休克', '严重过敏', '喉头水肿',
        '自杀', '自残', '服毒', '中毒', '烧伤严重', '大面积烧伤',
        '高热惊厥', '持续高烧', '严重脱水'
    ];

    const detectEmergency = (content) => {
        if (!content) return false;
        return EMERGENCY_KEYWORDS.some(k => content.includes(k));
    };

    // 记录每条消息对应的急救信息： { [msgId]: { status, hospitals, error, coords } }
    const [emergencyMap, setEmergencyMap] = useState({});

    // 获取用户地理位置
    const getUserLocation = () => new Promise((resolve, reject) => {
        if (!navigator.geolocation) {
            reject(new Error('当前浏览器不支持定位'));
            return;
        }
        navigator.geolocation.getCurrentPosition(
            pos => resolve({ lat: pos.coords.latitude, lon: pos.coords.longitude }),
            err => reject(err),
            { enableHighAccuracy: true, timeout: 8000, maximumAge: 60000 }
        );
    });

    // 通过 Overpass API（OpenStreetMap）搜索 5km 范围内正规医院
    const fetchNearbyHospitals = async (lat, lon) => {
        const radius = 5000; // 5km
        const query = `
            [out:json][timeout:15];
            (
              node["amenity"="hospital"](around:${radius},${lat},${lon});
              way["amenity"="hospital"](around:${radius},${lat},${lon});
              relation["amenity"="hospital"](around:${radius},${lat},${lon});
            );
            out center 20;
        `;
        const res = await fetch('https://overpass-api.de/api/interpreter', {
            method: 'POST',
            headers: { 'Content-Type': 'text/plain;charset=UTF-8' },
            body: query
        });
        if (!res.ok) throw new Error('医院查询服务不可用');
        const data = await res.json();
        const list = (data.elements || []).map(el => {
            const elat = el.lat || (el.center && el.center.lat);
            const elon = el.lon || (el.center && el.center.lon);
            const tags = el.tags || {};
            const name = tags['name:zh'] || tags.name || '未命名医院';
            // 计算直线距离（米）
            const dist = haversine(lat, lon, elat, elon);
            return {
                id: el.id,
                name,
                address: tags['addr:full'] || [tags['addr:province'], tags['addr:city'], tags['addr:district'], tags['addr:street'], tags['addr:housenumber']].filter(Boolean).join('') || '',
                phone: tags.phone || tags['contact:phone'] || '',
                emergency: tags.emergency === 'yes',
                lat: elat,
                lon: elon,
                distance: dist
            };
        }).filter(h => h.lat && h.lon && h.name !== '未命名医院');
        // 按距离排序，取前 3
        list.sort((a, b) => a.distance - b.distance);
        return list.slice(0, 3);
    };

    // 计算两点间球面距离（米）
    const haversine = (lat1, lon1, lat2, lon2) => {
        const toRad = d => (d * Math.PI) / 180;
        const R = 6371000;
        const dLat = toRad(lat2 - lat1);
        const dLon = toRad(lon2 - lon1);
        const a = Math.sin(dLat / 2) ** 2 +
            Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
        return 2 * R * Math.asin(Math.sqrt(a));
    };

    // 触发紧急检测与医院推荐
    const triggerEmergencyCheck = async (msgId, content) => {
        if (!detectEmergency(content)) return;
        if (emergencyMap[msgId]) return; // 已处理过

        setEmergencyMap(prev => ({ ...prev, [msgId]: { status: 'locating' } }));
        try {
            const coords = await getUserLocation();
            setEmergencyMap(prev => ({ ...prev, [msgId]: { status: 'searching', coords } }));
            const hospitals = await fetchNearbyHospitals(coords.lat, coords.lon);
            if (!hospitals || hospitals.length === 0) {
                setEmergencyMap(prev => ({ ...prev, [msgId]: { status: 'empty', coords } }));
            } else {
                setEmergencyMap(prev => ({ ...prev, [msgId]: { status: 'ok', coords, hospitals } }));
            }
        } catch (err) {
            console.error('紧急医院查询失败:', err);
            setEmergencyMap(prev => ({
                ...prev,
                [msgId]: { status: 'error', error: err.message || '定位或医院查询失败' }
            }));
        }
    };

    // 构造导航链接（高德地图 web，无需 key）
    const buildMapUrl = (h, from) => {
        const origin = from ? `${from.lon},${from.lat}` : '';
        return `https://uri.amap.com/navigation?from=${origin}&to=${h.lon},${h.lat}&toname=${encodeURIComponent(h.name)}&mode=car&src=jd-health`;
    };

    // 渲染紧急急救红色卡片
    const renderEmergencyCard = (msgId) => {
        const info = emergencyMap[msgId];
        if (!info) return null;
        return (
            <div className="mt-3 pt-3 border-t border-dashed border-red-200">
                <div className="rounded-xl border-2 border-red-400 bg-gradient-to-br from-red-50 to-red-100 p-3 shadow-sm">
                    <div className="flex items-center mb-2">
                        <div className="w-7 h-7 rounded-full bg-red-600 text-white flex items-center justify-center mr-2 animate-pulse">
                            <i className="fas fa-exclamation-triangle text-sm"></i>
                        </div>
                        <div className="flex-1">
                            <div className="text-red-700 font-bold text-[15px]">紧急就医提醒</div>
                            <div className="text-red-600 text-xs">您的症状可能较为紧急，请立即处理！</div>
                        </div>
                        <a
                            href="tel:120"
                            className="ml-2 px-3 py-1.5 rounded-lg bg-red-600 text-white text-sm font-semibold hover:bg-red-700 shadow"
                        >
                            <i className="fas fa-phone-alt mr-1"></i>拨打120
                        </a>
                    </div>

                    {info.status === 'locating' && (
                        <div className="text-sm text-red-700 flex items-center">
                            <i className="fas fa-spinner fa-spin mr-2"></i>正在获取您的位置...
                        </div>
                    )}
                    {info.status === 'searching' && (
                        <div className="text-sm text-red-700 flex items-center">
                            <i className="fas fa-spinner fa-spin mr-2"></i>正在搜索附近正规医院...
                        </div>
                    )}
                    {info.status === 'empty' && (
                        <div className="text-sm bg-white rounded-lg p-2 border border-red-200 text-red-700">
                            <i className="fas fa-info-circle mr-1"></i>
                            未能在您附近搜索到正规医院，请<strong className="mx-1">立即拨打 120</strong>寻求急救帮助！
                        </div>
                    )}
                    {info.status === 'error' && (
                        <div className="text-sm bg-white rounded-lg p-2 border border-red-200 text-red-700">
                            <i className="fas fa-info-circle mr-1"></i>
                            无法获取位置或查询医院（{info.error}），请<strong className="mx-1">立即拨打 120</strong>！
                        </div>
                    )}
                    {info.status === 'ok' && (
                        <div className="space-y-2">
                            <div className="text-xs text-red-600 mb-1">
                                <i className="fas fa-hospital mr-1"></i>为您推荐附近 {info.hospitals.length} 家医院（按距离排序）：
                            </div>
                            {info.hospitals.map((h, idx) => (
                                <div key={h.id} className="bg-white rounded-lg p-2.5 border border-red-200 flex items-start justify-between gap-2">
                                    <div className="flex-1 min-w-0">
                                        <div className="flex items-center">
                                            <span className="w-5 h-5 rounded-full bg-red-500 text-white text-[11px] flex items-center justify-center mr-2 flex-shrink-0">{idx + 1}</span>
                                            <span className="font-semibold text-gray-800 truncate">{h.name}</span>
                                            {h.emergency && (
                                                <span className="ml-2 text-[10px] px-1.5 py-0.5 rounded bg-red-100 text-red-600 flex-shrink-0">急诊</span>
                                            )}
                                        </div>
                                        <div className="text-xs text-gray-500 mt-1 flex items-center flex-wrap gap-x-3">
                                            <span><i className="fas fa-route mr-1 text-red-400"></i>约 {(h.distance / 1000).toFixed(2)} km</span>
                                            {h.address && <span className="truncate"><i className="fas fa-map-marker-alt mr-1 text-red-400"></i>{h.address}</span>}
                                        </div>
                                    </div>
                                    <div className="flex flex-col gap-1 flex-shrink-0">
                                        {h.phone && (
                                            <a href={`tel:${h.phone}`} className="text-xs px-2 py-1 rounded bg-green-500 text-white hover:bg-green-600 text-center">
                                                <i className="fas fa-phone-alt mr-1"></i>电话
                                            </a>
                                        )}
                                        <a
                                            href={buildMapUrl(h, info.coords)}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="text-xs px-2 py-1 rounded bg-red-500 text-white hover:bg-red-600 text-center"
                                        >
                                            <i className="fas fa-location-arrow mr-1"></i>导航
                                        </a>
                                    </div>
                                </div>
                            ))}
                            <div className="text-[11px] text-red-500 pt-1">
                                ⚠️ 如情况危急，请不要等待，<a href="tel:120" className="underline font-semibold">直接拨打 120</a>。
                            </div>
                        </div>
                    )}
                </div>
            </div>
        );
    };
    // ======================================================================

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
                            {msg.role === 'ai' ? (
                                (() => {
                                    // 打字中时先不提取，避免流式不完整内容误判
                                    const { cleanedContent, drugs } = msg.isTyping
                                        ? { cleanedContent: msg.content || '', drugs: [] }
                                        : extractDrugs(msg.content || '');
                                    return (
                                        <>
                                            <div className="text-[15px] leading-7 break-words text-left [&>*:first-child]:mt-0 [&>*:last-child]:mb-0 [&_p]:my-2 [&_ul]:my-2 [&_ul]:list-disc [&_ul]:pl-5 [&_ol]:my-2 [&_ol]:list-decimal [&_ol]:pl-5 [&_li]:my-1 [&_blockquote]:my-2 [&_blockquote]:border-l-4 [&_blockquote]:border-gray-200 [&_blockquote]:pl-3 [&_blockquote]:text-gray-600 [&_code]:px-1 [&_code]:py-0.5 [&_code]:rounded [&_code]:bg-gray-100 [&_pre]:my-2 [&_pre]:overflow-x-auto [&_pre]:rounded-lg [&_pre]:bg-gray-100 [&_pre]:p-3 [&_pre_code]:bg-transparent [&_h1]:my-3 [&_h1]:text-xl [&_h1]:font-semibold [&_h2]:my-3 [&_h2]:text-lg [&_h2]:font-semibold [&_h3]:my-2 [&_h3]:text-base [&_h3]:font-semibold">
                                                <ReactMarkdown remarkPlugins={[remarkGfm]}>{cleanedContent}</ReactMarkdown>
                                            </div>
                                            {renderEmergencyCard(msg.id)}
                                            {drugs.length > 0 && (
                                                <div className="mt-3 pt-3 border-t border-dashed border-gray-200">
                                                    <div className="flex items-center text-xs text-gray-500 mb-2">
                                                        <i className="fas fa-pills text-blue-500 mr-1.5"></i>
                                                        为您找到以下相关药品（点击前往京东查看）
                                                    </div>
                                                    <div className="flex flex-wrap gap-2">
                                                        {drugs.map((drug, idx) => (
                                                            <a
                                                                key={`${msg.id}-drug-${idx}`}
                                                                href={buildJdUrl(drug)}
                                                                target="_blank"
                                                                rel="noopener noreferrer"
                                                                className="group inline-flex items-center gap-2 px-3 py-2 bg-gradient-to-r from-red-50 to-orange-50 hover:from-red-100 hover:to-orange-100 border border-red-100 rounded-xl text-sm text-gray-800 transition-all shadow-sm hover:shadow"
                                                                title={`在京东查看「${drug}」`}
                                                            >
                                                                <span className="w-6 h-6 rounded-full bg-red-500 text-white flex items-center justify-center text-[10px] font-bold flex-shrink-0">
                                                                    JD
                                                                </span>
                                                                <span className="font-medium max-w-[140px] truncate">{drug}</span>
                                                                <i className="fas fa-external-link-alt text-[10px] text-gray-400 group-hover:text-red-500"></i>
                                                            </a>
                                                        ))}
                                                    </div>
                                                </div>
                                            )}
                                        </>
                                    );
                                })()
                            ) : (
                                <p className="text-[15px] leading-relaxed whitespace-pre-wrap">{msg.content}</p>
                            )}
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