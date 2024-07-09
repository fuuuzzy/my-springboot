package com.gspring.mybaties.annotation.parameterhandle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author xiangGang
 * @date 2023-01-27 14:49
 * @Description 字符串类型处理器
 */
public class StringTypeHandler implements TypeHandler {

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, Object value) throws SQLException {
        preparedStatement.setString(i, (String) value);
    }

    @Override
    public Object getResult(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getString(columnName);
    }
}
