package com.atguigu.java.ai.langchain4j.bean;

/**
 * 通用响应对象
 *
 * @param <T> 数据类型
 */
public class ApiResponse<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> failure(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(-1);
        response.setMessage(message);
        response.setData(null);
        return response;
    }

    public Integer getCode() {
        return code;
    }

    public ApiResponse<T> setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ApiResponse<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getData() {
        return data;
    }

    public ApiResponse<T> setData(T data) {
        this.data = data;
        return this;
    }
}