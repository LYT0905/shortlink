package com.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortlink.admin.common.biz.user.UserContext;
import com.shortlink.admin.common.convention.exception.ServiceException;
import com.shortlink.admin.common.convention.result.Result;
import com.shortlink.admin.dao.entity.GroupDO;
import com.shortlink.admin.dao.mapper.GroupMapper;
import com.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.shortlink.admin.remote.dto.request.RecycleBinPageReqDTO;
import com.shortlink.admin.remote.dto.response.ShortLinkPageRespDTO;
import com.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author LYT0905
 * @date 2024/03/07/16:45
 */

@Service(value = "recycleBinServiceImplByAdmin")
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
