package com.shortlink.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mark
 * @date 2024/2/27
 */

@RestController
public class UserController {
    @GetMapping("/api/short-link/admin/v1/user/{username}")
    public String getUserByUserName(@PathVariable String username){
        return "username:" + username;
    }
}
