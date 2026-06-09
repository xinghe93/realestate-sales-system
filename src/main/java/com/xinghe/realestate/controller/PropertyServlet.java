package com.xinghe.realestate.controller;

import com.xinghe.realestate.model.AppException;
import com.xinghe.realestate.model.LoginUser;
import com.xinghe.realestate.model.Property;
import com.xinghe.realestate.model.PropertyQuery;
import com.xinghe.realestate.model.PropertyStatus;
import com.xinghe.realestate.service.PropertyService;
import com.xinghe.realestate.util.JsonUtil;
import com.xinghe.realestate.util.RequestUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(name = "propertyServlet", urlPatterns = "/api/properties/*")
public class PropertyServlet extends BaseServlet {
    private final PropertyService propertyService = new PropertyService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long id = RequestUtil.pathId(request);
        LoginUser loginUser = currentUser(request);
        boolean adminView = loginUser != null && loginUser.isAdmin();
        if (id == null) {
            ok(response, propertyService.list(buildQuery(request, adminView), adminView));
            return;
        }
        ok(response, propertyService.get(id, adminView));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (RequestUtil.path(request).endsWith("/audit")) {
            audit(request, response);
            return;
        }
        LoginUser admin = requireAdmin(request);
        Property property = JsonUtil.read(request, Property.class);
        Long id = propertyService.create(property, admin.getId());
        ok(response, "房源已创建", Map.of("id", id));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        requireAdmin(request);
        Long id = RequestUtil.pathId(request);
        if (id == null) {
            throw new AppException("缺少房源 ID");
        }
        Property property = JsonUtil.read(request, Property.class);
        propertyService.update(id, property);
        ok(response, "房源已更新", null);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        requireAdmin(request);
        Long id = RequestUtil.pathId(request);
        if (id == null) {
            throw new AppException("缺少房源 ID");
        }
        propertyService.delete(id);
        ok(response, "房源已删除", null);
    }

    private void audit(HttpServletRequest request, HttpServletResponse response) throws IOException {
        requireAdmin(request);
        Long id = RequestUtil.pathId(request);
        if (id == null) {
            throw new AppException("缺少房源 ID");
        }
        AuditRequest auditRequest = JsonUtil.read(request, AuditRequest.class);
        propertyService.audit(id, auditRequest.status);
        ok(response, "审核状态已更新", null);
    }

    private PropertyQuery buildQuery(HttpServletRequest request, boolean adminView) {
        PropertyQuery query = new PropertyQuery();
        query.setKeyword(request.getParameter("keyword"));
        query.setLayout(request.getParameter("layout"));
        query.setRegion(request.getParameter("region"));
        query.setMinPrice(RequestUtil.decimalParam(request, "minPrice"));
        query.setMaxPrice(RequestUtil.decimalParam(request, "maxPrice"));
        query.setPage(RequestUtil.intParam(request, "page", 1));
        query.setPageSize(RequestUtil.intParam(request, "pageSize", 10));
        if (adminView && request.getParameter("status") != null && !request.getParameter("status").isBlank()) {
            try {
                query.setStatus(PropertyStatus.valueOf(request.getParameter("status")));
            } catch (IllegalArgumentException e) {
                throw new AppException("房源状态参数不合法");
            }
        }
        return query;
    }

    public static class AuditRequest {
        public PropertyStatus status;
    }
}
