package com.xinghe.realestate.model;

public enum Role {
    USER,
    ADMIN;

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
