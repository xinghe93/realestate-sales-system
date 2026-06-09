package com.xinghe.realestate.controller;

import com.xinghe.realestate.model.ApiResponse;
import com.xinghe.realestate.model.AppException;
import com.xinghe.realestate.model.LoginUser;
import com.xinghe.realestate.util.JsonUtil;
import com.xinghe.realestate.util.SessionKeys;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            super.service(request, response);
        } catch (AppException e) {
            JsonUtil.write(response, e.getStatusCode(), ApiResponse.fail(e.getMessage()));
        } catch (Exception e) {
            JsonUtil.write(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.fail("服务器内部错误：" + e.getMessage()));
        }
    }

    protected void ok(HttpServletResponse response, Object data) throws IOException {
        JsonUtil.write(response, HttpServletResponse.SC_OK, ApiResponse.ok(data));
    }

    protected void ok(HttpServletResponse response, String message, Object data) throws IOException {
        JsonUtil.write(response, HttpServletResponse.SC_OK, ApiResponse.ok(message, data));
    }

    protected LoginUser currentUser(HttpServletRequest request) {
        return (LoginUser) request.getSession().getAttribute(SessionKeys.LOGIN_USER);
    }

    protected LoginUser requireUser(HttpServletRequest request) {
        LoginUser loginUser = currentUser(request);
        if (loginUser == null) {
            throw new AppException(HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
        }
        return loginUser;
    }

    protected LoginUser requireAdmin(HttpServletRequest request) {
        LoginUser loginUser = requireUser(request);
        if (!loginUser.isAdmin()) {
            throw new AppException(HttpServletResponse.SC_FORBIDDEN, "无管理员权限");
        }
        return loginUser;
    }
}
