package com.xinghe.realestate.service;

import com.xinghe.realestate.dao.FavoriteDao;
import com.xinghe.realestate.dao.PropertyDao;
import com.xinghe.realestate.model.AppException;
import com.xinghe.realestate.model.Favorite;
import com.xinghe.realestate.model.Property;
import com.xinghe.realestate.model.PropertyStatus;

import java.util.List;

public class FavoriteService {
    private final FavoriteDao favoriteDao = new FavoriteDao();
    private final PropertyDao propertyDao = new PropertyDao();

    public List<Favorite> list(Long userId) {
        return favoriteDao.listByUser(userId);
    }

    public void add(Long userId, Long propertyId) {
        Property property = propertyDao.findById(propertyId)
                .orElseThrow(() -> new AppException(404, "房源不存在"));
        if (property.getStatus() != PropertyStatus.PUBLISHED) {
            throw new AppException("只能收藏已发布房源");
        }
        favoriteDao.add(userId, propertyId);
    }

    public void remove(Long userId, Long propertyId) {
        favoriteDao.remove(userId, propertyId);
    }
}
