package com.xinghe.realestate.service;

import com.xinghe.realestate.dao.UserDao;
import com.xinghe.realestate.model.AppException;
import com.xinghe.realestate.model.LoginUser;
import com.xinghe.realestate.model.Role;
import com.xinghe.realestate.model.User;
import com.xinghe.realestate.model.UserStatus;
import com.xinghe.realestate.util.SecurityUtil;

import java.time.Duration;
import java.time.LocalDateTime;

public class AuthService {
    private static final int LOCK_THRESHOLD = 5;
    private static final int LOCK_MINUTES = 10;

    private final UserDao userDao = new UserDao();

    public LoginUser login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new AppException("用户名和密码不能为空");
        }
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new AppException("用户名或密码错误"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(403, "账号已被禁用");
        }
        LocalDateTime now = LocalDateTime.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new AppException(423, lockedMessage(user.getLockedUntil(), now));
        }
        if (!SecurityUtil.verifyPassword(password, user.getPasswordHash())) {
            int failed = user.getFailedLoginCount() + 1;
            LocalDateTime lockedUntil = failed >= LOCK_THRESHOLD ? now.plusMinutes(LOCK_MINUTES) : null;
            userDao.updateLoginSecurity(user.getId(), failed, lockedUntil);
            if (lockedUntil != null) {
                throw new AppException(423, lockedMessage(lockedUntil, now));
            }
            throw new AppException("用户名或密码错误");
        }

        userDao.updateLoginSecurity(user.getId(), 0, null);
        return new LoginUser(user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }

    public Long register(User user, String password) {
        validateRegister(user, password);
        if (userDao.findByUsername(user.getUsername()).isPresent()) {
            throw new AppException("用户名已存在");
        }
        user.setPasswordHash(SecurityUtil.hashPassword(password));
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        return userDao.create(user);
    }

    private void validateRegister(User user, String password) {
        if (user == null || isBlank(user.getUsername()) || isBlank(password)) {
            throw new AppException("用户名和密码不能为空");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String lockedMessage(LocalDateTime lockedUntil, LocalDateTime now) {
        long seconds = Math.max(1, Duration.between(now, lockedUntil).toSeconds());
        long minutes = (long) Math.ceil(seconds / 60.0);
        return "登录失败次数过多，请等待 " + minutes + " 分钟后再试";
    }
}
