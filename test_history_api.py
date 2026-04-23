#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
import json

def test_chat_history_api():
    """测试聊天记录API"""
    
    # 假设我们有一个memoryId，实际使用时应该从数据库或前端获取
    memory_id = "test-memory-id"
    
    # 测试获取聊天记录API
    url = f"http://localhost:8080/api/chat/history/{memory_id}"
    
    try:
        print(f"测试API: {url}")
        response = requests.get(url)
        
        if response.status_code == 200:
            data = response.json()
            print("API响应成功:")
            print(json.dumps(data, indent=2, ensure_ascii=False))
            
            if data.get("code") == 0 and data.get("data"):
                print("\n聊天记录格式验证:")
                for i, msg in enumerate(data["data"]):
                    role = msg.get("role", "unknown")
                    content = msg.get("content", "")
                    print(f"消息 {i+1}: 角色={role}, 内容='{content}'")
            else:
                print("没有获取到聊天记录或API返回错误")
        else:
            print(f"API请求失败，状态码: {response.status_code}")
            print(response.text)
            
    except Exception as e:
        print(f"测试失败: {e}")

if __name__ == "__main__":
    test_chat_history_api()