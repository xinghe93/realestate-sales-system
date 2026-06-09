package com.xinghe.realestate.controller;

import com.xinghe.realestate.model.AppException;
import com.xinghe.realestate.model.LoginUser;
import com.xinghe.realestate.service.FavoriteService;
import com.xinghe.realestate.util.RequestUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "favoriteServlet", urlPatterns = "/api/favorites/*")
public class FavoriteServlet extends BaseServlet {
    private final FavoriteService favoriteService = new FavoriteService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LoginUser user = requireUser(request);
        ok(response, favoriteService.list(user.getId()));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LoginUser user = requireUser(request);
        Long propertyId = RequestUtil.pathId(request);
        if (propertyId == null) {
            throw new AppException("缺少房源 ID");
        }
        favoriteService.add(user.getId(), propertyId);
        ok(response, "收藏成功", null);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LoginUser user = requireUser(request);
        Long propertyId = RequestUtil.pathId(request);
        if (propertyId == null) {
            throw new AppException("缺少房源 ID");
        }
        favoriteService.remove(user.getId(), propertyId);
        ok(response, "已取消收藏", null);
    }
}
