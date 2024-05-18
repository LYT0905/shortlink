package com.shortlink.project.service.impl;

/**
 * @author LYT0905
 * @date 2024/03/07/12:59
 */


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.project.common.constant.RedisKeyConstant;
import com.shortlink.project.dao.entity.ShortLinkDO;
import com.shortlink.project.dao.mapper.RecycleBinMapper;
import com.shortlink.project.dao.mapper.ShortLinkMapper;
import com.shortlink.project.dto.request.RecycleBinPageReqDTO;
import com.shortlink.project.dto.request.RecycleBinRecoverReqDTO;
import com.shortlink.project.dto.request.RecycleBinRemoveReqDTO;
import com.shortlink.project.dto.request.RecycleBinSaveReqDTO;
import com.shortlink.project.dto.response.ShortLinkPageRespDTO;
import com.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 回收站功能实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<RecycleBinMapper, ShortLinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ShortLinkMapper shortLinkMapper;
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
        stringRedisTemplate.delete(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }

    /**
     * 回收站短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询返回结果
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(RecycleBinPageReqDTO requestParam) {
        IPage<ShortLinkDO> result = shortLinkMapper.pageRecycleBinLink(requestParam);
        return result.convert((each) ->
                {
                    ShortLinkPageRespDTO resultPage = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
                    resultPage.setDomain("http://" + resultPage.getDomain());
                    return resultPage;
                }
        );
    }

    /**
     * 回收站短链接恢复功能
     * @param requestParam 请求参数
     * @return void
     */
    @Override
    public void recoverShortLinkRecycleBin(RecycleBinRecoverReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl());
        ShortLinkDO shortLinkDO = ShortLinkDO.builder().enableStatus(0).build();
        baseMapper.update(shortLinkDO, updateWrapper);
        stringRedisTemplate.delete(String.format(RedisKeyConstant.GOTO_SHORT_LINK_IS_NULL_KEY, requestParam.getFullShortUrl()));
    }

    /**
     * 回收站中的短链接删除
     * @param requestParam 请求参数
     * @return void
     */
    @Override
    public void removeShortLinkRecycleBin(RecycleBinRemoveReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelTime, 0L)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO delShortLinkDO = ShortLinkDO.builder()
                .delTime(System.currentTimeMillis())
                .build();
        delShortLinkDO.setDelFlag(1);
        baseMapper.update(delShortLinkDO, updateWrapper);
    }
}
