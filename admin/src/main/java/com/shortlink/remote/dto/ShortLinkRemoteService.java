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
import com.shortlink.remote.dto.request.ShortLinkCreateReqDTO;
import com.shortlink.remote.dto.request.ShortLinkPageReqDTO;
import com.shortlink.remote.dto.response.ShortLinkCreateRespDTO;
import com.shortlink.remote.dto.response.ShortLinkPageRespDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台调用远程服务
 */

public interface ShortLinkRemoteService {

    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam){
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        Map<String, Object> map = new HashMap<>();
        map.put("gid", requestParam.getGid());
        map.put("current", requestParam.getCurrent());
        map.put("size", requestParam.getSize());
        String resultPage = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", map);
        return JSON.parseObject(resultPage, new TypeReference<>() {
        });
    }

    default Result<List<ShortLinkGroupRespDTO>> listGroupShortLinkCount(List<String> requestParam){
        Map<String, Object> map = new HashMap<>();
        map.put("requestParam", requestParam);
        String resultCount = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/count", map);
        return JSON.parseObject(resultCount, new TypeReference<>() {
        });
    }
}
