package com.igeeksky.xredis.examples;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.Objects;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/11
 */
public class Response<T> {

    public static final int OK = 1;
    public static final int ERROR = 0;

    private final int code;
    private final String msg;
    private final T data;

    public Response() {
        this(OK, "ok");
    }

    public Response(int code, String msg) {
        this(code, msg, null);
    }

    public Response(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Response<T> ok() {
        return new Response<>(OK, "ok");
    }

    public static <T> Response<T> ok(T data) {
        return new Response<>(OK, "ok", data);
    }

    public static <T> Response<T> error(String msg) {
        return new Response<>(ERROR, msg);
    }

    public static <T> Response<T> of(int code, String msg, T data) {
        return new Response<>(code, msg, data);
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Response<?> response)) return false;

        return getCode() == response.getCode() && Objects.equals(getMsg(), response.getMsg()) && Objects.equals(getData(), response.getData());
    }

    @Override
    public int hashCode() {
        int result = getCode();
        result = 31 * result + Objects.hashCode(getMsg());
        result = 31 * result + Objects.hashCode(getData());
        return result;
    }

    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}