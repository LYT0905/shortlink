package com.shortlink.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortlink.dao.entity.UserDO;
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
     * @param username
     * @return UserRespDto
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否存在
     * @param username
     * @return 存在返回false，不存在返回ture
     */
    Boolean hasUserName(String username);
}
