package com.xinghe.realestate.filter;

import com.xinghe.realestate.dao.UserDao;
import com.xinghe.realestate.model.ApiResponse;
import com.xinghe.realestate.model.LoginUser;
import com.xinghe.realestate.util.JsonUtil;
import com.xinghe.realestate.util.SessionKeys;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(filterName = "authFilter", urlPatterns = "/api/*")
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (isPublicEndpoint(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        LoginUser loginUser = (LoginUser) httpRequest.getSession().getAttribute(SessionKeys.LOGIN_USER);
        if (loginUser == null) {
            JsonUtil.write(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, ApiResponse.fail("请先登录"));
            return;
        }
        if (!userDao.isActive(loginUser.getId())) {
            httpRequest.getSession().invalidate();
            JsonUtil.write(httpResponse, HttpServletResponse.SC_FORBIDDEN, ApiResponse.fail("账号已被禁用"));
            return;
        }
        chain.doFilter(request, response);
    }

    private final UserDao userDao = new UserDao();

    private boolean isPublicEndpoint(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        if (path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/captcha")) {
            return true;
        }
        return "GET".equalsIgnoreCase(request.getMethod()) && path.startsWith("/api/properties");
    }
}
