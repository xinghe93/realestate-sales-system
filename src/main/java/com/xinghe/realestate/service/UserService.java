package com.xinghe.realestate.service;

import com.xinghe.realestate.dao.UserDao;
import com.xinghe.realestate.model.AppException;
import com.xinghe.realestate.model.Role;
import com.xinghe.realestate.model.User;
import com.xinghe.realestate.model.UserStatus;
import com.xinghe.realestate.util.SecurityUtil;

import java.util.List;

public class UserService {
    private final UserDao userDao = new UserDao();

    public User get(Long id) {
        return userDao.findById(id).orElseThrow(() -> new AppException(404, "用户不存在"));
    }

    public List<User> list() {
        return userDao.list();
    }

    public Long createByAdmin(User user, String password) {
        if (user == null || isBlank(user.getUsername()) || isBlank(password)) {
            throw new AppException("用户名和密码不能为空");
        }
        if (userDao.findByUsername(user.getUsername()).isPresent()) {
            throw new AppException("用户名已存在");
        }
        user.setPasswordHash(SecurityUtil.hashPassword(password));
        user.setRole(user.getRole() == null ? Role.USER : user.getRole());
        user.setStatus(user.getStatus() == null ? UserStatus.ACTIVE : user.getStatus());
        return userDao.create(user);
    }

    public void updateProfile(Long id, User input) {
        User user = get(id);
        user.setRealName(input.getRealName());
        user.setPhone(input.getPhone());
        user.setEmail(input.getEmail());
        userDao.updateProfile(user);
    }

    public void updateByAdmin(Long id, User input) {
        User user = get(id);
        user.setRealName(input.getRealName());
        user.setPhone(input.getPhone());
        user.setEmail(input.getEmail());
        user.setRole(input.getRole() == null ? user.getRole() : input.getRole());
        user.setStatus(input.getStatus() == null ? user.getStatus() : input.getStatus());
        userDao.updateByAdmin(user);
    }

    public void disable(Long id) {
        User user = get(id);
        user.setStatus(UserStatus.DISABLED);
        userDao.updateByAdmin(user);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
