package com.gspring.mybaties.annotation.parameterhandle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author xiangGang
 * @date 2023-01-27 14:50
 * @Description 整型类型处理器
 */
public class IntegerTypeHandler implements TypeHandler {

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, Object value) throws SQLException {
        preparedStatement.setInt(i, (Integer) value);
    }

    @Override
    public Object getResult(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getInt(columnName);
    }
}
