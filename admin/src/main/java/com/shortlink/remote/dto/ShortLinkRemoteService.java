package com.shortlink.remote.dto;

/**
 * @author LYT0905
 * @date 2024/03/05/12:41
 */

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortlink.common.convention.result.Result;
import com.shortlink.dto.response.ShortLinkGroupRespDTO;
import com.shortlink.remote.dto.request.RecycleBinSaveReqDTO;
import com.shortlink.remote.dto.request.ShortLinkCreateReqDTO;
import com.shortlink.remote.dto.request.ShortLinkPageReqDTO;
import com.shortlink.remote.dto.request.ShortLinkUpdateReqDTO;
import com.shortlink.remote.dto.response.ShortLinkCreateRespDTO;
import com.shortlink.remote.dto.response.ShortLinkPageRespDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台调用远程服务
 */

public interface ShortLinkRemoteService {

    /**
     * 创建短链接
     * @param requestParam 短链接创建参数
     * @return ShortLinkCreateRespDTO
     */
    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam){
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 短链接分页查询
     * @param requestParam 短链接分页查询参数
     * @return 短链接分页查询返回结果
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        Map<String, Object> map = new HashMap<>();
        map.put("gid", requestParam.getGid());
        map.put("current", requestParam.getCurrent());
        map.put("size", requestParam.getSize());
        String resultPage = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", map);
        return JSON.parseObject(resultPage, new TypeReference<>() {
        });
    }

    /**
     * 查询当前组下的短链接数量
     * @param requestParam 分组标识
     * @return 短链接数量返回
     */
    default Result<List<ShortLinkGroupRespDTO>> listGroupShortLinkCount(List<String> requestParam){
        Map<String, Object> map = new HashMap<>();
        map.put("requestParam", requestParam);
        String resultCount = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/count", map);
        return JSON.parseObject(resultCount, new TypeReference<>() {
        });
    }

    /**
     * 修改短链接
     * @param requestParam 修改短链接请求参数
     * @return void
     */
    default void updateShortLink(ShortLinkUpdateReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/update", JSON.toJSONString(requestParam));
    }

    /**
     * 根据链接获取标题
     * @param url 链接
     * @return 链接标题
     */
    default Result<String> getTitleByUrl(String url){
        String resultTitle = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/title?url=" + url);
        return JSON.parseObject(resultTitle, new TypeReference<>() {
        });
    }

    /**
     * 将短链接移至到回收站
     * @param requestParam 请求参数
     * @return void
     */
    default void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/save", JSON.toJSONString(requestParam));
    }
}
