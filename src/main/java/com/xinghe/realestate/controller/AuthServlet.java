package com.xinghe.realestate.controller;

import com.xinghe.realestate.model.ApiResponse;
import com.xinghe.realestate.model.AppException;
import com.xinghe.realestate.model.LoginUser;
import com.xinghe.realestate.model.User;
import com.xinghe.realestate.service.AuthService;
import com.xinghe.realestate.util.JsonUtil;
import com.xinghe.realestate.util.RequestUtil;
import com.xinghe.realestate.util.SessionKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@WebServlet(name = "authServlet", urlPatterns = "/api/auth/*")
public class AuthServlet extends BaseServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        switch (RequestUtil.path(request)) {
            case "/me" -> ok(response, currentUser(request));
            case "/captcha" -> captcha(request, response);
            default -> throw new AppException(404, "接口不存在");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        switch (RequestUtil.path(request)) {
            case "/login" -> login(request, response);
            case "/logout" -> logout(request, response);
            case "/register" -> register(request, response);
            default -> throw new AppException(404, "接口不存在");
        }
    }

    private void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int left = ThreadLocalRandom.current().nextInt(1, 10);
        int right = ThreadLocalRandom.current().nextInt(1, 10);
        request.getSession().setAttribute(SessionKeys.CAPTCHA_ANSWER, String.valueOf(left + right));
        ok(response, Map.of("question", left + " + " + right + " = ?"));
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LoginRequest loginRequest = JsonUtil.read(request, LoginRequest.class);
        request.getSession().removeAttribute(SessionKeys.LOGIN_USER);
        String answer = (String) request.getSession().getAttribute(SessionKeys.CAPTCHA_ANSWER);
        request.getSession().removeAttribute(SessionKeys.CAPTCHA_ANSWER);
        if (answer == null || loginRequest.captcha == null || !answer.equals(loginRequest.captcha.trim())) {
            throw new AppException("验证码错误");
        }

        try {
            LoginUser loginUser = authService.login(loginRequest.username, loginRequest.password);
            request.getSession().setAttribute(SessionKeys.LOGIN_USER, loginUser);
            ok(response, "登录成功", loginUser);
        } catch (AppException e) {
            JsonUtil.write(response, e.getStatusCode(), ApiResponse.fail(e.getMessage()));
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().invalidate();
        ok(response, "已退出登录", null);
    }

    private void register(HttpServletRequest request, HttpServletResponse response) throws IOException {
        RegisterRequest registerRequest = JsonUtil.read(request, RegisterRequest.class);
        User user = new User();
        user.setUsername(registerRequest.username);
        user.setRealName(registerRequest.realName);
        user.setPhone(registerRequest.phone);
        user.setEmail(registerRequest.email);
        Long id = authService.register(user, registerRequest.password);
        ok(response, "注册成功", Map.of("id", id));
    }

    public static class LoginRequest {
        public String username;
        public String password;
        public String captcha;
    }

    public static class RegisterRequest {
        public String username;
        public String password;
        public String realName;
        public String phone;
        public String email;
    }
}
