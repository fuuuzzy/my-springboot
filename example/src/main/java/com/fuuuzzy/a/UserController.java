package com.fuuuzzy.a;

import com.gspring.gannotation.Autowired;
import com.gspring.gannotation.springmvc.Controller;
import com.gspring.gannotation.springmvc.GetMapping;
import com.gspring.gannotation.springmvc.PathVariable;

import java.util.List;

/**
 * @author xiangGang
 * @date 2023-01-30 22:33
 * @Description
 */
@Controller("/user")
public class UserController {


    @Autowired
    private UserService userService;

    @GetMapping("/user")
    public List<User> getUser() {
        return userService.getUser();
    }

    @GetMapping("/user/{id}/{name}")
    public List<User> getUser1(@PathVariable("id") String id, @PathVariable("name") String name) {
        return userService.getUser();
    }
}
