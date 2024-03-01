package com.shortlink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortlink.dao.entity.GroupDO;
import com.shortlink.dao.mapper.GroupMapper;
import com.shortlink.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author LYT0905
 * @date 2024/03/01/16:05
 */

@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    private final GroupService groupService;
}
