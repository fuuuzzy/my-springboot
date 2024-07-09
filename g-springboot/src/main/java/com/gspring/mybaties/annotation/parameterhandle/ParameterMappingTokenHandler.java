package com.gspring.mybaties.annotation.parameterhandle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiangGang
 * @date 2023-01-27 13:45
 * @Description 参数映射处理器
 */
public class ParameterMappingTokenHandler implements TokenHandler {

    /**
     * SQL参数映射集合
     */
    private final List<ParameterMapping> sqlParameterMappings = new ArrayList<>();

    @Override
    public String handleToken(String content) {
        sqlParameterMappings.add(new ParameterMapping(content));
        return "?";
    }

    public List<ParameterMapping> getSqlParameterMappings() {
        return sqlParameterMappings;
    }
}
