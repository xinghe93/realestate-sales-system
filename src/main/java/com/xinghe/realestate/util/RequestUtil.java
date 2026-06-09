package com.xinghe.realestate.util;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;

public final class RequestUtil {
    private RequestUtil() {
    }

    public static String path(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || path.isBlank() ? "/" : path;
    }

    public static Long pathId(HttpServletRequest request) {
        String path = path(request);
        if (path.equals("/")) {
            return null;
        }
        String clean = path.startsWith("/") ? path.substring(1) : path;
        if (clean.contains("/")) {
            clean = clean.substring(0, clean.indexOf('/'));
        }
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static int intParam(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static BigDecimal decimalParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
