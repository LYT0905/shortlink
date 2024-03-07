package com.shortlink.service.impl;

/**
 * @author LYT0905
 * @date 2024/03/07/12:59
 */


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.dao.entity.ShortLinkDO;
import com.shortlink.dao.mapper.RecycleBinMapper;
import com.shortlink.dto.request.RecycleBinSaveReqDTO;
import com.shortlink.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.shortlink.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

/**
 * 回收站功能实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<RecycleBinMapper, ShortLinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;
    /**
     * 将短链接移至到回收站
     * @param requestParam 请求参数
     * @return void
     */
    @Override
    public void recycleBinSave(RecycleBinSaveReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid());
        // 将短链接状态设置为禁用
        ShortLinkDO shortLinkDO = ShortLinkDO.builder().enableStatus(1).build();
        baseMapper.update(shortLinkDO, updateWrapper);
        // 将缓存数据删掉
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }
}
