package com.xinghe.realestate.controller;

import com.xinghe.realestate.model.AppException;
import com.xinghe.realestate.model.LoginUser;
import com.xinghe.realestate.model.User;
import com.xinghe.realestate.service.UserService;
import com.xinghe.realestate.util.JsonUtil;
import com.xinghe.realestate.util.RequestUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "userServlet", urlPatterns = "/api/users/*")
public class UserServlet extends BaseServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (RequestUtil.path(request).equals("/me")) {
            LoginUser loginUser = requireUser(request);
            ok(response, userService.get(loginUser.getId()));
            return;
        }
        requireAdmin(request);
        ok(response, userService.list());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        requireAdmin(request);
        CreateUserRequest createUserRequest = JsonUtil.read(request, CreateUserRequest.class);
        User user = createUserRequest.toUser();
        Long id = userService.createByAdmin(user, createUserRequest.password);
        ok(response, "用户已创建", Map.of("id", id));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User input = JsonUtil.read(request, User.class);
        if (RequestUtil.path(request).equals("/me")) {
            LoginUser loginUser = requireUser(request);
            userService.updateProfile(loginUser.getId(), input);
            ok(response, "个人信息已更新", null);
            return;
        }

        requireAdmin(request);
        Long id = RequestUtil.pathId(request);
        if (id == null) {
            throw new AppException("缺少用户 ID");
        }
        userService.updateByAdmin(id, input);
        ok(response, "用户信息已更新", null);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        requireAdmin(request);
        Long id = RequestUtil.pathId(request);
        if (id == null) {
            throw new AppException("缺少用户 ID");
        }
        userService.disable(id);
        ok(response, "用户已禁用", null);
    }

    public static class CreateUserRequest {
        public String username;
        public String password;
        public String realName;
        public String phone;
        public String email;
        public com.xinghe.realestate.model.Role role;
        public com.xinghe.realestate.model.UserStatus status;

        public User toUser() {
            User user = new User();
            user.setUsername(username);
            user.setRealName(realName);
            user.setPhone(phone);
            user.setEmail(email);
            user.setRole(role);
            user.setStatus(status);
            return user;
        }
    }
}
