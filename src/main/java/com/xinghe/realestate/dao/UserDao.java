package com.xinghe.realestate.dao;

import com.xinghe.realestate.model.Role;
import com.xinghe.realestate.model.User;
import com.xinghe.realestate.model.UserStatus;
import com.xinghe.realestate.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    public Optional<User> findByUsername(String username) {
        String sql = "select * from users where username = ?";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query user by username", e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long id) {
        String sql = "select * from users where id = ?";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query user by id", e);
        }
        return Optional.empty();
    }

    public List<User> list() {
        String sql = "select * from users order by created_at desc";
        List<User> users = new ArrayList<>();
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list users", e);
        }
    }

    public Long create(User user) {
        String sql = """
                insert into users(username, password_hash, real_name, phone, email, role, status)
                values (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getRealName());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getEmail());
            statement.setString(6, user.getRole().name());
            statement.setString(7, user.getStatus().name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create user", e);
        }
        throw new IllegalStateException("Failed to read generated user id");
    }

    public void updateProfile(User user) {
        String sql = "update users set real_name = ?, phone = ?, email = ? where id = ?";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getRealName());
            statement.setString(2, user.getPhone());
            statement.setString(3, user.getEmail());
            statement.setLong(4, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update user profile", e);
        }
    }

    public void updateByAdmin(User user) {
        String sql = "update users set real_name = ?, phone = ?, email = ?, role = ?, status = ? where id = ?";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getRealName());
            statement.setString(2, user.getPhone());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getRole().name());
            statement.setString(5, user.getStatus().name());
            statement.setLong(6, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update user by admin", e);
        }
    }

    public boolean isActive(Long id) {
        String sql = "select status from users where id = ?";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return UserStatus.valueOf(rs.getString("status")) == UserStatus.ACTIVE;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to check user status", e);
        }
        return false;
    }

    public long countActiveAdmins() {
        String sql = "select count(*) from users where role = 'ADMIN' and status = 'ACTIVE'";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count active admins", e);
        }
    }

    public void updateLoginSecurity(Long id, int failedLoginCount, LocalDateTime lockedUntil) {
        String sql = "update users set failed_login_count = ?, locked_until = ? where id = ?";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, failedLoginCount);
            if (lockedUntil == null) {
                statement.setTimestamp(2, null);
            } else {
                statement.setTimestamp(2, Timestamp.valueOf(lockedUntil));
            }
            statement.setLong(3, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update login security fields", e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRealName(rs.getString("real_name"));
        user.setPhone(rs.getString("phone"));
        user.setEmail(rs.getString("email"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setStatus(UserStatus.valueOf(rs.getString("status")));
        user.setFailedLoginCount(rs.getInt("failed_login_count"));
        user.setLockedUntil(toLocalDateTime(rs.getTimestamp("locked_until")));
        user.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        user.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return user;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
