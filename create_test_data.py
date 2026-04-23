#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
import json

def create_test_conversation():
    """创建测试会话数据"""
    
    # 测试创建会话并发送消息
    memory_id = "test-conversation-456"
    
    # 发送用户消息
    user_message_url = "http://localhost:8080/api/diagnostic/chat"
    user_message_data = {
        "message": "测试记忆功能",
        "memoryId": memory_id
    }
    
    print("发送用户消息...")
    try:
        response = requests.post(user_message_url, json=user_message_data)
        if response.status_code == 200:
            print("用户消息发送成功")
            data = response.json()
            print(json.dumps(data, indent=2, ensure_ascii=False))
        else:
            print(f"用户消息发送失败: {response.status_code}")
            print(response.text)
    except Exception as e:
        print(f"发送用户消息失败: {e}")
    
    # 测试获取历史记录
    history_url = f"http://localhost:8080/api/chat/history/{memory_id}"
    print(f"\n获取历史记录: {history_url}")
    
    try:
        response = requests.get(history_url)
        if response.status_code == 200:
            data = response.json()
            print("历史记录获取成功:")
            print(json.dumps(data, indent=2, ensure_ascii=False))
            
            if data.get("code") == 0 and data.get("data"):
                print("\n聊天记录内容:")
                for i, msg in enumerate(data["data"]):
                    role = msg.get("role", "unknown")
                    content = msg.get("content", "")
                    print(f"消息 {i+1}: 角色={role}, 内容='{content}'")
            else:
                print("没有获取到聊天记录")
        else:
            print(f"获取历史记录失败: {response.status_code}")
            print(response.text)
            
    except Exception as e:
        print(f"获取历史记录失败: {e}")

if __name__ == "__main__":
    create_test_conversation()