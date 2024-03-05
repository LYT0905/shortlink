package com.shortlink.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.common.biz.user.UserContext;
import com.shortlink.common.convention.result.Result;
import com.shortlink.dao.entity.GroupDO;
import com.shortlink.dao.mapper.GroupMapper;
import com.shortlink.dto.request.ShortLinkGroupUpdateReqDTO;
import com.shortlink.dto.request.ShortLinkGroupUpdateSortReqDTO;
import com.shortlink.dto.response.ShortLinkGroupRespDTO;
import com.shortlink.remote.dto.ShortLinkRemoteService;
import com.shortlink.service.GroupService;
import com.shortlink.toolkit.RandomGenerator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author LYT0905
 * @date 2024/03/01/16:05
 */

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    /**
     * 新增短链接分组
     * @param groupName 请求参数
     */
    @Override
    public void saveGroup(String groupName) {
        String gid;
        do {
            gid = RandomGenerator.generateRandom();
        } while (hasGid(gid));

        GroupDO groupDO = GroupDO.builder()
                .name(groupName)
                .username(UserContext.getUsername())
                .sortOrder(0)
                .gid(gid)
                .build();

        baseMapper.insert(groupDO);
    }

    /**
     * 查询短链接分组
     * @return 短链接分组
     */
    @Override
    public List<ShortLinkGroupRespDTO> lisGroup() {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupDOS = baseMapper.selectList(queryWrapper);
        // 查询每组的短链接数量
        Result<List<ShortLinkGroupRespDTO>> listResult = shortLinkRemoteService.listGroupShortLinkCount(groupDOS.stream()
                .map(GroupDO::getGid).collect(Collectors.toList()));
        List<ShortLinkGroupRespDTO> shortLinkGroupRespDTO = BeanUtil.copyToList(groupDOS, ShortLinkGroupRespDTO.class);
        // 设置每组的短链接数量
        shortLinkGroupRespDTO.forEach((each) -> {
            Optional<ShortLinkGroupRespDTO> first = listResult.getData().stream()
                            .filter(item -> Objects.equals(item.getGid(), each.getGid())).findFirst();
            first.ifPresent(item -> each.setShortLinkCount(first.get().getShortLinkCount()));
        });
        return shortLinkGroupRespDTO;
    }

    /**
     * 修改短链接分组名称
     * @param requestParam 修改名称与修改对象
     */
    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, requestParam.getGid());
        GroupDO groupDO = new GroupDO();
        groupDO.setName(requestParam.getName());
        baseMapper.update(groupDO, queryWrapper);
    }

    /**
     * 删除短链接分组
     * @param gid 短链接分组标识
     */
    @Override
    public void deleteGroup(String gid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername, UserContext.getUsername());
        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        baseMapper.update(groupDO, queryWrapper);
    }

    /**
     * 判断是否存在
     * @param gid
     * @return 存在返回true, 不存在返回false
     */
    private boolean hasGid(String gid){
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getName, UserContext.getUsername());
        return baseMapper.selectOne(queryWrapper) != null;
    }

    @Override
    public void updateGroupSort(List<ShortLinkGroupUpdateSortReqDTO> requestParam) {
        requestParam.forEach(each -> {
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(each.getSortOrder())
                    .build();
            LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getGid, each.getGid())
                    .eq(GroupDO::getDelFlag, 0);
            baseMapper.update(groupDO, queryWrapper);
        });
    }
}
