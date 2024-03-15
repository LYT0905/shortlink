package com.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shortlink.project.dao.entity.ShortLinkDO;
import com.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.shortlink.project.dto.request.ShortLinkBatchCreateReqDTO;
import com.shortlink.project.dto.request.ShortLinkCreateReqDTO;
import com.shortlink.project.dto.request.ShortLinkPageReqDTO;
import com.shortlink.project.dto.request.ShortLinkUpdateReqDTO;
import com.shortlink.project.dto.response.ShortLinkBatchCreateRespDTO;
import com.shortlink.project.dto.response.ShortLinkCreateRespDTO;
import com.shortlink.project.dto.response.ShortLinkGroupRespDTO;
import com.shortlink.project.dto.response.ShortLinkPageRespDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * @author LYT0905
 * @date 2024/03/03/21:41
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短链接
     * @param requestParam 短链接创建参数
     * @return ShortLinkCreateRespDTO
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    /**
     * 短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询返回结果
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 查询当前组下的短链接数量
     * @param requestParam 分组标识
     * @return 短链接数量返回
     */
    List<ShortLinkGroupRespDTO> listGroupShortLinkCount(List<String> requestParam);

    /**
     * 修改短链接
     * @param requestParam 修改短链接请求参数
     * @return void
     */
    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    /**
     * 短链接跳转
     * @param shortUri 获取跳转的短链接
     * @param request 请求
     * @param response 响应
     */
    void restoreUri(String shortUri, ServletRequest request, ServletResponse response) throws IOException;

    /**
     * 批量创建短链接
     * @param requestParam 请求创建参数
     * @return 返回参数
     */
    ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam);

    /**
     * 短链接统计
     *
     * @param fullShortUrl         完整短链接
     * @param gid                  分组标识
     * @param shortLinkStatsRecord 短链接统计实体参数
     */
    void shortLinkStats(String fullShortUrl, String gid, ShortLinkStatsRecordDTO shortLinkStatsRecord);
}
