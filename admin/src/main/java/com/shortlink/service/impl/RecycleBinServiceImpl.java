package com.shortlink.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.common.biz.user.UserContext;
import com.shortlink.common.convention.exception.ServiceException;
import com.shortlink.common.convention.result.Result;
import com.shortlink.dao.entity.GroupDO;
import com.shortlink.dao.mapper.GroupMapper;
import com.shortlink.remote.ShortLinkActualRemoteService;
import com.shortlink.remote.dto.request.RecycleBinPageReqDTO;
import com.shortlink.remote.dto.response.ShortLinkPageRespDTO;
import com.shortlink.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author LYT0905
 * @date 2024/03/07/16:45
 */

@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;
    private final GroupMapper groupMapper;

    @Override
    public Result<Page<ShortLinkPageRespDTO>> pageRecycleShortLink(RecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOS = groupMapper.selectList(queryWrapper);
        if(CollUtil.isEmpty(groupDOS)){
            throw new ServiceException("用户分组信息不存在");
        }
        requestParam.setGidList(groupDOS.stream().map(GroupDO::getGid).toList());
        return shortLinkActualRemoteService.pageRecycleBinShortLink(requestParam.getGidList(),
                requestParam.getCurrent(), requestParam.getSize());
    }
}
