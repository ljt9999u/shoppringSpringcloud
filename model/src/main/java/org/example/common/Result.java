package org.example.common;

import lombok.Data;

/**
 * 统一返回结果（泛型版本）
 */
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    /**
     * 成功 - 带数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功 - 无数据
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("成功");
        return result;
    }

    /**
     * 失败 - 默认 500 错误码
     */
    public static <T> Result<T> fail(String message) {
        Result<T> r = new Result<>();
        r.setCode(500);
        r.setMessage(message);
        return r;
    }

    /**
     * 失败 - 自定义错误码
     */
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
