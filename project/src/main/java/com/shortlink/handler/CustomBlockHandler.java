package com.shortlink.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.shortlink.common.convention.result.Result;
import com.shortlink.dto.request.ShortLinkCreateReqDTO;
import com.shortlink.dto.response.ShortLinkCreateRespDTO;

/**
 * 自定义流控策略
 *
 */
public class CustomBlockHandler {

    public static Result<ShortLinkCreateRespDTO> createShortLinkBlockHandlerMethod(ShortLinkCreateReqDTO requestParam, BlockException exception) {
        return new Result<ShortLinkCreateRespDTO>().setCode("B100000").setMessage("当前访问网站人数过多，请稍后再试...");
    }
}