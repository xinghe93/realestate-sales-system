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

    public PageResult<Property> list(PropertyQuery query, boolean adminView) {
        if (!adminView) {
            query.setStatus(PropertyStatus.PUBLISHED);
        }
        return propertyDao.list(query);
    }

    public Property get(Long id, boolean adminView) {
        Property property = propertyDao.findById(id)
                .orElseThrow(() -> new AppException(404, "房源不存在"));
        if (!adminView && property.getStatus() != PropertyStatus.PUBLISHED) {
            throw new AppException(404, "房源不存在");
        }
        return property;
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
        propertyDao.findById(id).orElseThrow(() -> new AppException(404, "房源不存在"));
        validate(property);
        property.setId(id);
        if (property.getStatus() == null) {
            property.setStatus(PropertyStatus.PUBLISHED);
        }
        propertyDao.update(property);
    }

    public void audit(Long id, PropertyStatus status) {
        propertyDao.findById(id).orElseThrow(() -> new AppException(404, "房源不存在"));
        if (status != PropertyStatus.PUBLISHED && status != PropertyStatus.OFFLINE) {
            throw new AppException("审核状态不合法");
        }
        propertyDao.updateStatus(id, status);
    }

    public void delete(Long id) {
        propertyDao.findById(id).orElseThrow(() -> new AppException(404, "房源不存在"));
        propertyDao.delete(id);
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
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
