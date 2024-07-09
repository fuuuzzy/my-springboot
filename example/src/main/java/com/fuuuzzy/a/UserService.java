package com.fuuuzzy.a;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.gspring.gannotation.Autowired;
import com.gspring.gannotation.Service;

import java.util.List;

/**
 * @author xiangGang
 * @date 2023-01-30 22:34
 * @Description
 */
@Service
public class UserService {
    private static final Log LOG = LogFactory.getLog(UserService.class);
    @Autowired
    private UserMapper userMapper;

    public List<User> getUser() {
        List<User> user = userMapper.getUser();
        LOG.info("getUser");
        LOG.info(user.toString());
        return user;
    }
}
