package com.igeeksky.xredis.examples;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
class UserControllerTest {

    private static final String HOST = "http://localhost:8080";

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static final HttpResponse.BodyHandler<String> RESPONSE_HANDLER =
            response -> HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JavaType RESPONSE_USER_TYPE = MAPPER.getTypeFactory().constructParametricType(Response.class, User.class);

    private static final JacksonCodec<Response<User>> RESPONSE_USER_CODEC = new JacksonCodec<>(MAPPER, RESPONSE_USER_TYPE);

    @Test
    void createUser() {
        String user = new User(1L, "Patrick.Lau", 18).toString();
        Response<User> response = createUser(user);
        System.out.printf("%s : %s\n", "createUser", response.getData());
    }

    @Test
    void getUserById() {
        User user = getUser(1L).getData();
        System.out.printf("%s : %s\n", "getUserById", user);
    }

    /**
     * 发送获取用户请求
     *
     * @param id 用户ID
     * @return 包含用户信息的响应对象，类型为 {@code Response<User>}，其中 User 为获取到的用户信息
     */
    private static Response<User> getUser(long id) {
        String url = "/user/get/" + id;
        String body = sendAndReceive(getRequest(url));
        return RESPONSE_USER_CODEC.decode(body);
    }

    /**
     * 发送创建用户请求
     *
     * @param user 用户信息(JSON String)
     * @return 包含用户信息的响应对象，类型为 {@code Response<User>}，其中 User 为创建的用户信息
     */
    private static Response<User> createUser(String user) {
        // 创建用户请求的URL路径
        String url = "/user/create";
        // 发送请求并接收响应，请求体即为用户信息字符串，经过序列化后发送
        String body = sendAndReceive(postRequest(url, user));
        // 解码响应体，返回包含用户信息的响应对象
        return RESPONSE_USER_CODEC.decode(body);
    }

    /**
     * 发送HTTP请求并接收响应
     *
     * @param request HTTP请求对象
     * @return 响应体，类型为 byte[]
     */
    private static String sendAndReceive(HttpRequest request) {
        try {
            return CLIENT.send(request, RESPONSE_HANDLER).body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建GET请求
     *
     * @param url 请求URL路径
     * @return HttpRequest对象，用于发送GET请求
     */
    private static HttpRequest getRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(HOST + url))
                .GET()
                .build();
    }

    /**
     * 创建POST请求
     *
     * @param url  请求URL路径
     * @param body 请求体，类型为 JSON格式的字符串
     * @return HttpRequest对象，用于发送POST请求
     */
    private static HttpRequest postRequest(String url, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(HOST + url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

}