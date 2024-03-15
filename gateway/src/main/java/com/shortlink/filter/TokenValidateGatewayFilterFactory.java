package com.shortlink.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.shortlink.admin.config.Config;
import com.shortlink.admin.dto.GatewayErrorResult;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

;
/**
 * SpringCloud Gateway Token 拦截器
 *
 */
@Component
public class TokenValidateGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private final StringRedisTemplate stringRedisTemplate;

    public TokenValidateGatewayFilterFactory(StringRedisTemplate stringRedisTemplate) {
        super(Config.class);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 应用网关过滤器，用于认证和授权处理。
     *
     * @param config 配置对象，包含白名单路径等配置信息。
     * @return 返回一个GatewayFilter实例，用于处理请求的认证和授权逻辑。
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestPath = request.getPath().toString();
            String requestMethod = request.getMethod().name();
            // 判断是否是白名单地址
            if (!isPathInWhiteList(requestPath, requestMethod, config.getWhitePathList())) {
                String username = request.getHeaders().getFirst("username");
                String token = request.getHeaders().getFirst("token");
                Object userInfo;
                // 判断是否登录
                if (StringUtils.hasText(username) && StringUtils.hasText(token) && (userInfo = stringRedisTemplate.opsForHash().get("short-link:login:" + username, token)) != null) {
                    JSONObject userInfoJsonObject = JSON.parseObject(userInfo.toString());
                    ServerHttpRequest.Builder builder = exchange.getRequest().mutate().headers(httpHeaders -> {
                        // 设置已登录用户的用户ID和真实姓名
                        httpHeaders.set("userId", userInfoJsonObject.getString("id"));
                        httpHeaders.set("realName", URLEncoder.encode(userInfoJsonObject.getString("realName"), StandardCharsets.UTF_8));
                    });
                    // 登录成功，继续处理请求
                    return chain.filter(exchange.mutate().request(builder.build()).build());
                }
                // 没有登录直接报401错误
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.writeWith(Mono.fromSupplier(() -> {
                    DataBufferFactory bufferFactory = response.bufferFactory();
                    GatewayErrorResult resultMessage = GatewayErrorResult.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("Token validation error")
                            .build();
                    return bufferFactory.wrap(JSON.toJSONString(resultMessage).getBytes());
                }));
            }
            // 请求在白名单内，直接通过
            return chain.filter(exchange);
        };
    }

    /**
     * 判断请求路径是否在白名单中。
     *
     * @param requestPath 请求的路径。
     * @param requestMethod 请求的方法，如GET、POST。
     * @param whitePathList 白名单路径列表。
     * @return 如果请求路径在白名单中或者特定的路径与方法组合匹配，则返回true；否则返回false。
     */
    private boolean isPathInWhiteList(String requestPath, String requestMethod, List<String> whitePathList) {
        // 判断白名单路径列表是否为空，不为空则判断是否有路径以请求路径开始，或者判断特定路径与方法是否匹配
        return (!CollectionUtils.isEmpty(whitePathList) && whitePathList.stream().anyMatch(requestPath::startsWith))
                || (Objects.equals(requestPath, "/api/short-link/admin/v1/user") && Objects.equals(requestMethod, "POST"));
    }
}