package com.gspring.mybaties.annotation.parameterhandle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author xiangGang
 * @date 2023-01-27 14:48
 * @Description 类型处理器
 */
public interface TypeHandler {

    /**
     * 设置参数
     *
     * @param preparedStatement preparedStatement
     * @param i                 位置
     * @param value             值
     * @throws SQLException SQLException
     */
    void setParameter(PreparedStatement preparedStatement, int i, Object value) throws SQLException;

    /**
     * 获取结果
     *
     * @param resultSet  resultSet
     * @param columnName columnName
     * @return {@link Object} 结果
     * @throws SQLException SQLException
     */
    Object getResult(ResultSet resultSet, String columnName) throws SQLException;
}
