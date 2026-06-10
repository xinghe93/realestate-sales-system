package com.xinghe.realestate.dao;

import com.xinghe.realestate.model.PageResult;
import com.xinghe.realestate.model.Property;
import com.xinghe.realestate.model.PropertyQuery;
import com.xinghe.realestate.model.PropertyStatus;
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

public class PropertyDao {
    public PageResult<Property> list(PropertyQuery query) {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(query, params);
        String countSql = "select count(*) from properties p " + where;
        String listSql = """
                select p.*, u.real_name contact_name, u.phone contact_phone
                from properties p
                left join users u on u.id = p.created_by
                %s
                order by p.updated_at desc
                limit ? offset ?
                """.formatted(where);

        try (Connection connection = DbUtil.getConnection()) {
            long total = count(connection, countSql, params);
            List<Property> items = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(listSql)) {
                bindParams(statement, params);
                statement.setInt(params.size() + 1, query.getPageSize());
                statement.setInt(params.size() + 2, query.getOffset());
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        items.add(mapProperty(rs));
                    }
                }
            }
            return new PageResult<>(items, total, query.getPage(), query.getPageSize());
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list properties", e);
        }
    }

    public Optional<Property> findById(Long id) {
        String sql = """
                select p.*, u.real_name contact_name, u.phone contact_phone
                from properties p
                left join users u on u.id = p.created_by
                where p.id = ?
                """;
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapProperty(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query property", e);
        }
        return Optional.empty();
    }

    public Long create(Property property) {
        String sql = """
                insert into properties(title, region, address, layout, price, area, map_x, map_y, image_url, description, status, created_by)
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindEditableProperty(statement, property);
            statement.setString(11, property.getStatus().name());
            statement.setLong(12, property.getCreatedBy());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create property", e);
        }
        throw new IllegalStateException("Failed to read generated property id");
    }

    public int update(Property property) {
        String sql = """
                update properties
                set title = ?, region = ?, address = ?, layout = ?, price = ?, area = ?,
                    map_x = ?, map_y = ?, image_url = ?, description = ?, status = ?
                where id = ?
                """;
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindEditableProperty(statement, property);
            statement.setString(11, property.getStatus().name());
            statement.setLong(12, property.getId());
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update property", e);
        }
    }

    public int updateStatus(Long id, PropertyStatus status) {
        String sql = "update properties set status = ? where id = ?";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, id);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update property status", e);
        }
    }

    public int delete(Long id) {
        String sql = "delete from properties where id = ?";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete property", e);
        }
    }

    private String buildWhere(PropertyQuery query, List<Object> params) {
        StringBuilder sql = new StringBuilder("where 1 = 1");
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            sql.append(" and (p.title like ? or p.address like ? or p.description like ?)");
            String keyword = "%" + query.getKeyword().trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }
        if (query.getLayout() != null && !query.getLayout().isBlank()) {
            sql.append(" and p.layout = ?");
            params.add(query.getLayout());
        }
        if (query.getRegion() != null && !query.getRegion().isBlank()) {
            sql.append(" and p.region = ?");
            params.add(query.getRegion());
        }
        if (query.getMinPrice() != null) {
            sql.append(" and p.price >= ?");
            params.add(query.getMinPrice());
        }
        if (query.getMaxPrice() != null) {
            sql.append(" and p.price <= ?");
            params.add(query.getMaxPrice());
        }
        if (query.getStatus() != null) {
            sql.append(" and p.status = ?");
            params.add(query.getStatus().name());
        }
        return sql.toString();
    }

    private long count(Connection connection, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParams(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    private void bindParams(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }

    private void bindEditableProperty(PreparedStatement statement, Property property) throws SQLException {
        statement.setString(1, property.getTitle());
        statement.setString(2, property.getRegion());
        statement.setString(3, property.getAddress());
        statement.setString(4, property.getLayout());
        statement.setBigDecimal(5, property.getPrice());
        statement.setBigDecimal(6, property.getArea());
        statement.setBigDecimal(7, property.getMapX());
        statement.setBigDecimal(8, property.getMapY());
        statement.setString(9, property.getImageUrl());
        statement.setString(10, property.getDescription());
    }

    private Property mapProperty(ResultSet rs) throws SQLException {
        Property property = new Property();
        property.setId(rs.getLong("id"));
        property.setTitle(rs.getString("title"));
        property.setRegion(rs.getString("region"));
        property.setAddress(rs.getString("address"));
        property.setLayout(rs.getString("layout"));
        property.setPrice(rs.getBigDecimal("price"));
        property.setArea(rs.getBigDecimal("area"));
        property.setMapX(rs.getBigDecimal("map_x"));
        property.setMapY(rs.getBigDecimal("map_y"));
        property.setImageUrl(rs.getString("image_url"));
        property.setDescription(rs.getString("description"));
        property.setStatus(PropertyStatus.valueOf(rs.getString("status")));
        property.setCreatedBy(rs.getLong("created_by"));
        property.setContactName(rs.getString("contact_name"));
        property.setContactPhone(rs.getString("contact_phone"));
        property.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        property.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return property;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
