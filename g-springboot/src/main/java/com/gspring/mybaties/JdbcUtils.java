package com.gspring.mybaties;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

/**
 * @author xiangGang
 * @date 2023-01-19 18:04
 * @Description JDBC工具类
 */
public class JdbcUtils {
    
    /**
     * 数据源
     */
    private static DataSource ds;

    private JdbcUtils() {
    }

    static {
        try {
            //加载配置文件
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("jdbc.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            ds = DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据源
     *
     * @return {@link DataSource} 数据源
     */
    public static DataSource getDataSource() {
        return ds;
    }

    /**
     * 获取连接
     *
     * @return {@link Connection} 连接
     * @throws Exception 异常
     */
    public static Connection getConnection() throws Exception {
        return ds.getConnection();
    }

}
