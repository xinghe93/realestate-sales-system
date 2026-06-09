package com.xinghe.realestate.util;

import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class DbUtil {
    private static final DruidDataSource DATA_SOURCE = createDataSource();

    private DbUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    public static DataSource getDataSource() {
        return DATA_SOURCE;
    }

    private static DruidDataSource createDataSource() {
        Properties properties = loadProperties();
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(properties.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        dataSource.setUrl(required(properties, "db.url"));
        dataSource.setUsername(required(properties, "db.username"));
        dataSource.setPassword(properties.getProperty("db.password", ""));
        dataSource.setInitialSize(intProperty(properties, "db.pool.initialSize", 2));
        dataSource.setMinIdle(intProperty(properties, "db.pool.minIdle", 2));
        dataSource.setMaxActive(intProperty(properties, "db.pool.maxActive", 10));
        dataSource.setMaxWait(longProperty(properties, "db.pool.maxWait", 60000L));
        dataSource.setValidationQuery(properties.getProperty("db.pool.validationQuery", "select 1"));
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        return dataSource;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream in = DbUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("Missing db.properties in src/main/resources");
            }
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load db.properties", e);
        }
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required database config: " + key);
        }
        return value;
    }

    private static int intProperty(Properties properties, String key, int defaultValue) {
        return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
    }

    private static long longProperty(Properties properties, String key, long defaultValue) {
        return Long.parseLong(properties.getProperty(key, String.valueOf(defaultValue)));
    }
}
