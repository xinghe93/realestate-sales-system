package com.xinghe.realestate.model;

public class LoginUser {
    private Long id;
    private String username;
    private String realName;
    private Role role;

    public LoginUser() {
    }

    public LoginUser(Long id, String username, String realName, Role role) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return role != null && role.isAdmin();
    }
}
