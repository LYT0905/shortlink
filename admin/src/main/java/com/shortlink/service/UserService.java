package com.shortlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortlink.dao.entity.UserDO;
import com.shortlink.dto.request.UserLoginReqDTO;
import com.shortlink.dto.request.UserRegisterReqDTO;
import com.shortlink.dto.request.UserUpdateReqDTO;
import com.shortlink.dto.response.UserLoginRespDTO;
import com.shortlink.dto.response.UserRespDTO;

/**
 * @author LYT0905
 * @date 2024/02/27/17:30
 */

/**
 * user服务层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名返回用户
     * @param username 用户名
     * @return UserRespDto
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否存在
     * @param username 用户名
     * @return 存在返回true，不存在返回false
     */
    Boolean hasUserName(String username);

    /**
     * 用户注册
     * @param requestParam 用户注册信息
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 修改用户信息
     * @param requestParam 请求参数
     */
    void update(UserUpdateReqDTO requestParam);

    /**
     * 用户登录
     * @param requestParam 用户登录请求参数
     * @return 用户token
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 判断用户是否登录
     * @param username 用户名
     * @param token 用户token
     * @return 登录返回true，否则返回false
     */
    Boolean checkLogin(String username, String token);

    /**
     * 用户退出登录
     * @param username 用户名
     * @param token token标识
     * @return void
     */
    void logout(String username, String token);
}
