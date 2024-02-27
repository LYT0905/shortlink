package com.shortlink.service.impl;

/**
 * @author LYT0905
 * @date 2024/02/27/17:30
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.common.convention.exception.ClientException;
import com.shortlink.common.enums.UserErrorCodeEnums;
import com.shortlink.dao.entity.UserDO;
import com.shortlink.dao.mapper.UserMapper;
import com.shortlink.dto.response.UserRespDTO;
import com.shortlink.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * userservice实现层
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    @Override
    public UserRespDTO getUserByUsername(String username) {
        // 构建条件构造器
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        // 用户不存在，通过全局异常处理器抛出异常
        if(userDO == null){
            throw  new ClientException(UserErrorCodeEnums.USER_NULL);
        }
        // 将查到的数据封装到respDto对象上面，将数据进行返回
        UserRespDTO userRespDTO = new UserRespDTO();
        BeanUtils.copyProperties(userDO, userRespDTO);
        return userRespDTO;
    }
}
