package com.shortlink.admin.remote.dto.request;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * @author LYT0905
 * @date 2024/03/07/16:43
 */

/**
 * 回收站分页查询
 */
@Data
public class RecycleBinPageReqDTO extends Page {

    /**
     * 分组标识
     */
    private List<String> gidList;
}
