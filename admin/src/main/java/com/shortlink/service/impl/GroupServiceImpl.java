package com.shortlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.dao.entity.GroupDO;
import com.shortlink.dao.mapper.GroupMapper;
import com.shortlink.service.GroupService;
import com.shortlink.toolkit.RandomGenerator;
import org.springframework.stereotype.Service;

/**
 * @author LYT0905
 * @date 2024/03/01/16:05
 */

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    @Override
    public void saveGroup(String groupName) {
        String gid;
        do {
            gid = RandomGenerator.generateRandom();
        } while (hasGid(gid));

        GroupDO groupDO = GroupDO.builder()
                .name(groupName)
                .sortOrder(0)
                .gid(gid)
                .build();

        baseMapper.insert(groupDO);
    }

    private boolean hasGid(String gid){
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                // TODO 设置用户名
                .eq(GroupDO::getName, null);
        return baseMapper.selectOne(queryWrapper) != null;
    }
}
