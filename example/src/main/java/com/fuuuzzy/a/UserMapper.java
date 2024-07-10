package com.fuuuzzy.a;

import com.gspring.mybaties.annotation.Mapper;
import com.gspring.mybaties.annotation.Select;

import java.util.List;

/**
 * @author xiangGang
 * @date 2023-01-30 22:39
 * @Description
 */
@Mapper
public interface UserMapper {
    /**
     * 获取用户
     *
     * @return {@link List<User>} 用户列表
     */
    @Select("select * from user")
    List<User> getUser();
}
