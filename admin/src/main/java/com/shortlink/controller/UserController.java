package com.shortlink.controller;

import com.shortlink.dto.response.UserRespDTO;
import com.shortlink.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


/**
 * 用户管理
 */
@RestController
// 此注解作用和@Autowired一样
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查询用户
     * @param username
     * @return UserRespDTO
     */
    @GetMapping("/api/short-link/admin/v1/user/{username}")
    public UserRespDTO getUserByUserName(@PathVariable String username){
        return userService.getUserByUsername(username);
    }
}
