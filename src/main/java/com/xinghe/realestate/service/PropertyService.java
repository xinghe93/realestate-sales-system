package com.xinghe.realestate.service;

import com.xinghe.realestate.dao.PropertyDao;
import com.xinghe.realestate.model.AppException;
import com.xinghe.realestate.model.PageResult;
import com.xinghe.realestate.model.Property;
import com.xinghe.realestate.model.PropertyQuery;
import com.xinghe.realestate.model.PropertyStatus;

import java.math.BigDecimal;

public class PropertyService {
    private final PropertyDao propertyDao = new PropertyDao();
    private static final BigDecimal MAP_MIN = BigDecimal.ZERO;
    private static final BigDecimal MAP_MAX = BigDecimal.valueOf(100);

    public PageResult<Property> list(PropertyQuery query) {
        return propertyDao.list(query);
    }

    public Property get(Long id) {
        return propertyDao.findById(id)
                .orElseThrow(() -> new AppException(404, "房源不存在"));
    }

    public Long create(Property property, Long adminId) {
        validate(property);
        property.setCreatedBy(adminId);
        if (property.getStatus() == null) {
            property.setStatus(PropertyStatus.PUBLISHED);
        }
        return propertyDao.create(property);
    }

    public void update(Long id, Property property) {
        validate(property);
        property.setId(id);
        if (property.getStatus() == null) {
            property.setStatus(PropertyStatus.PUBLISHED);
        } else if (property.getStatus() != PropertyStatus.PUBLISHED
                && property.getStatus() != PropertyStatus.OFFLINE) {
            throw new AppException("房源状态不合法");
        }
        if (propertyDao.update(property) == 0) {
            throw new AppException(404, "房源不存在");
        }
    }

    public void audit(Long id, PropertyStatus status) {
        if (status != PropertyStatus.PUBLISHED && status != PropertyStatus.OFFLINE) {
            throw new AppException("审核状态不合法");
        }
        if (propertyDao.updateStatus(id, status) == 0) {
            throw new AppException(404, "房源不存在");
        }
    }

    public void delete(Long id) {
        if (propertyDao.delete(id) == 0) {
            throw new AppException(404, "房源不存在");
        }
    }

    private void validate(Property property) {
        if (isBlank(property.getTitle())) {
            throw new AppException("房源标题不能为空");
        }
        if (isBlank(property.getRegion())) {
            throw new AppException("区域不能为空");
        }
        if (isBlank(property.getLayout())) {
            throw new AppException("户型不能为空");
        }
        if (property.getPrice() == null || property.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("价格必须大于 0");
        }
        if (property.getArea() == null || property.getArea().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("面积必须大于 0");
        }
        validateMapPosition(property.getMapX(), "地图横坐标");
        validateMapPosition(property.getMapY(), "地图纵坐标");
    }

    private void validateMapPosition(BigDecimal value, String label) {
        if (value == null) {
            throw new AppException("请选择房源在地图上的位置");
        }
        if (value.compareTo(MAP_MIN) < 0 || value.compareTo(MAP_MAX) > 0) {
            throw new AppException(label + "必须在 0 到 100 之间");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
