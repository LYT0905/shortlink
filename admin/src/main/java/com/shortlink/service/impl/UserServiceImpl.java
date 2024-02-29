package com.shortlink.service.impl;

/**
 * @author LYT0905
 * @date 2024/02/27/17:30
 */

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.common.convention.exception.ClientException;
import com.shortlink.common.enums.UserErrorCodeEnums;
import com.shortlink.dao.entity.UserDO;
import com.shortlink.dao.mapper.UserMapper;
import com.shortlink.dto.request.UserRegisterReqDTO;
import com.shortlink.dto.request.UserUpdateReqDTO;
import com.shortlink.dto.response.UserRespDTO;
import com.shortlink.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static com.shortlink.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.shortlink.common.enums.UserErrorCodeEnums.USER_NAME_EXIST;
import static com.shortlink.common.enums.UserErrorCodeEnums.USER_SAVE_ERROR;

/**
 * userservice实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return userRespDTO 浏览器响应数据
     */
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

    /**
     * 查询用户名是否存在(使用布隆过滤器，防止数据库被打满)
     * @param username 用户名
     * @return 存在返回true，不存在返回false
     */
    @Override
    public Boolean hasUserName(String username) {
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    /**
     * 用户注册
     * @param requestParam 用户注册信息
     */
    @Override
    public void register(UserRegisterReqDTO requestParam) {
        // 先去布隆过滤器判断是否存在
        if(hasUserName(requestParam.getUsername())){
            throw new ClientException(USER_NAME_EXIST);
        }
        // 用分布式锁防止短时间内恶意提交重复用户名
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());
        try {
            if(lock.tryLock()){
                int insert = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
                if(insert < 1){
                    throw new ClientException(USER_SAVE_ERROR);
                }
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                return;
            }
            throw new ClientException(USER_NAME_EXIST);
        }finally {
            lock.unlock();
        }
    }

    /**
     * 根据用户名修改用户信息
     * @param requestParam 请求参数
     */
    @Override
    public void update(UserUpdateReqDTO requestParam) {
        // TODO 验证当前用户是否为登录用户
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class).
                eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam, UserDO.class), queryWrapper);
    }
}
