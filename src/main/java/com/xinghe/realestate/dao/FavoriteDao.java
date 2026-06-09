package com.xinghe.realestate.dao;

import com.xinghe.realestate.model.Favorite;
import com.xinghe.realestate.model.Property;
import com.xinghe.realestate.model.PropertyStatus;
import com.xinghe.realestate.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDao {
    public List<Favorite> listByUser(Long userId) {
        String sql = """
                select f.id favorite_id, f.user_id, f.property_id, f.created_at favorite_created_at,
                       p.*, u.real_name contact_name, u.phone contact_phone
                from favorites f
                join properties p on p.id = f.property_id
                left join users u on u.id = p.created_by
                where f.user_id = ?
                order by f.created_at desc
                """;
        List<Favorite> favorites = new ArrayList<>();
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Favorite favorite = new Favorite();
                    favorite.setId(rs.getLong("favorite_id"));
                    favorite.setUserId(rs.getLong("user_id"));
                    favorite.setPropertyId(rs.getLong("property_id"));
                    favorite.setCreatedAt(toLocalDateTime(rs.getTimestamp("favorite_created_at")));
                    favorite.setProperty(mapProperty(rs));
                    favorites.add(favorite);
                }
            }
            return favorites;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list favorites", e);
        }
    }

    public void add(Long userId, Long propertyId) {
        String sql = "insert ignore into favorites(user_id, property_id) values (?, ?)";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, propertyId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to add favorite", e);
        }
    }

    public void remove(Long userId, Long propertyId) {
        String sql = "delete from favorites where user_id = ? and property_id = ?";
        try (Connection connection = DbUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setLong(2, propertyId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to remove favorite", e);
        }
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
