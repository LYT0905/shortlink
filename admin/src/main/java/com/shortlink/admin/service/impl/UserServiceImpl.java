package com.shortlink.admin.service.impl;

/**
 * @author LYT0905
 * @date 2024/02/27/17:30
 */

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.admin.common.biz.user.UserContext;
import com.shortlink.admin.common.convention.exception.ClientException;
import com.shortlink.admin.common.enums.UserErrorCodeEnums;
import com.shortlink.admin.dao.entity.UserDO;
import com.shortlink.admin.dao.mapper.UserMapper;
import com.shortlink.admin.dto.request.UserLoginReqDTO;
import com.shortlink.admin.dto.request.UserRegisterReqDTO;
import com.shortlink.admin.dto.request.UserUpdateReqDTO;
import com.shortlink.admin.dto.response.UserLoginRespDTO;
import com.shortlink.admin.dto.response.UserRespDTO;
import com.shortlink.admin.service.GroupService;
import com.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static com.shortlink.admin.common.enums.UserErrorCodeEnums.*;

/**
 * userservice实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final GroupService groupService;

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return userRespDTO 浏览器响应数据
     */
    @Override
    public UserRespDTO getUserByUsername(String username) {
        // 构建条件构造器
        QueryWrapper<UserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).eq("del_flag", 0);
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
        if (!lock.tryLock()){
            throw new ClientException(USER_NAME_EXIST);
        }
        try {
            int insert = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
            if(insert < 1){
                throw new ClientException(USER_SAVE_ERROR);
            }
            userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
            groupService.saveGroup(requestParam.getUsername(), "默认分组");
        }catch (DuplicateKeyException exception){
            throw new ClientException(USER_EXIST);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 根据用户名修改用户信息
     * @param requestParam 请求参数
     */
    @Override
    public void update(UserUpdateReqDTO requestParam) {
        if (!ObjectUtil.equals(requestParam.getUsername(), UserContext.getUsername())){
            throw new ClientException("当前用户修改信息失败");
        }
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class).
                eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam, UserDO.class), queryWrapper);
    }

    /**
     * 用户登录
     * @param requestParam 用户登录请求参数
     * @return UserLoginRespDTO
     */
    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class).
                eq(UserDO::getUsername, requestParam.getUsername()).
                eq(UserDO::getPassword, requestParam.getPassword());
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if(userDO == null){
            throw new ClientException(USER_LOGIN_ERROR);
        }
        // 如果已经登录，直接返回token(避免之前只能在一个地方登录的问题)
        Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries(USER_LOGIN_KEY + requestParam.getUsername());
        if(CollUtil.isNotEmpty(hasLoginMap)){
            // 更新已登录用户的token有效期
            stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), 30L, TimeUnit.MINUTES);
            String token = hasLoginMap.keySet().stream().findFirst().map(Object::toString)
                    .orElseThrow(() -> new ClientException("用户登录信息错误"));
            return new UserLoginRespDTO(token);
        }
        /**
         * 防止重复提交登录导致接口压力过大
         * Hash结构
         * KEY: login_用户名
         * Value:
         *   KEY: token标识
         *   Value: JSON字符串
         */
        // 生成一个唯一的UUID作为token，防止重复登录提交
        String uuid = UUID.randomUUID().toString();
        // 将用户登录信息（包含用户详细信息）存储到Redis中，以token为key，过期时间为30分钟
        stringRedisTemplate.opsForHash().put(USER_LOGIN_KEY + requestParam.getUsername(), uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), 30L, TimeUnit.MINUTES);
        // 返回生成的token给客户端
        return new UserLoginRespDTO(uuid);
    }

    /**
     * 判断用户是否登录
     * @param username 用户名
     * @param token 用户token
     * @return 登录返回true，否则返回false
     */
    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token) != null;
    }

    /**
     * 用户退出登录
     * @param username 用户名
     * @param token token标识
     * @return void
     */
    @Override
    public void logout(String username, String token) {
        // 判断是否已登录
        if(checkLogin(username, token)){
            stringRedisTemplate.delete(USER_LOGIN_KEY + username);
            return;
        }
        throw new ClientException(USER_LOGIN_ERROR);
    }
}
