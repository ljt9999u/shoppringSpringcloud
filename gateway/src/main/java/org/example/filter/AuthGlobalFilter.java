package org.example.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 网关全局 JWT 鉴权过滤器
 * 1. 放行：CORS 预检(OPTIONS)、登录注册、健康检查、GET 方式的商品/分类/品牌/商家/评价浏览
 * 2. 其余接口必须携带 Authorization: Bearer <token>
 * 3. 校验通过后，把 userId / username / roleCode 写入请求头透传给下游微服务
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /** 与 model 模块 JwtUtil 的签名密钥保持一致 */
    private static final String SECRET = "itheima";

    /** 下游服务读取登录用户信息的请求头 */
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";
    public static final String HEADER_ROLE_CODE = "X-Role-Code";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // 1. CORS 预检请求直接放行
        if (method == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // 2. 白名单放行
        if (isWhiteList(path, method)) {
            return chain.filter(exchange);
        }

        // 3. 取 token
        String token = request.getHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty()) {
            return writeUnauthorized(exchange, "未登录，请先登录");
        }

        // 4. 校验并解析 token
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token);
            Map<String, Object> claims = jwt.getClaim("claims").asMap();
            String userId = claims.get("userId") == null ? null : String.valueOf(claims.get("userId"));
            String username = claims.get("username") == null ? "" : String.valueOf(claims.get("username"));
            String roleCode = claims.get("roleCode") == null ? "" : String.valueOf(claims.get("roleCode"));

            // 5. 透传用户身份信息给下游
            ServerHttpRequest mutated = request.mutate()
                    .header(HEADER_USER_ID, userId == null ? "" : userId)
                    .header(HEADER_USERNAME, username)
                    .header(HEADER_ROLE_CODE, roleCode)
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            return writeUnauthorized(exchange, "token 无效或已过期，请重新登录");
        }
    }

    /**
     * 白名单：无需登录即可访问
     */
    private boolean isWhiteList(String path, HttpMethod method) {
        // 各服务健康检查
        if (path.endsWith("/health")) {
            return true;
        }
        // 用户登录、注册
        if ("/api/user/login".equals(path) || "/api/user/register".equals(path)) {
            return true;
        }
        // 支付宝服务器异步回调（无 JWT，由支付宝验签保证安全）
        if ("/api/oride/pay/alipay/notify".equals(path)) {
            return true;
        }
        // GET 浏览类接口：商品、分类、品牌、商家店铺、商品评价
        if (method == HttpMethod.GET) {
            if (path.startsWith("/api/product/") && !path.startsWith("/api/product/audit/")) {
                return true;
            }
            if (path.startsWith("/api/category")) {
                return true;
            }
            if (path.startsWith("/api/brand")) {
                return true;
            }
            if (path.startsWith("/api/merchant/")) {
                return true;
            }
            if (path.startsWith("/api/oride/comment/")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回 401 JSON
     */
    private Mono<Void> writeUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 优先级尽量高，先鉴权再做后续过滤
        return -100;
    }
}
