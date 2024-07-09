package com.gspring.mybaties.annotation.parameterhandle;

/**
 * @author xiangGang
 * @date 2023-01-27 13:45
 * @Description 参数映射处理器
 */
public interface TokenHandler {
    /**
     * 处理token
     *
     * @param content token内容
     * @return {@link String} 处理后的内容
     */
    String handleToken(String content);
}
