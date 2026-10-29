package com.box.utils;

import com.box.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT 工具类 —— 从请求头解析当前用户身份
 * <p>
 * 所有 Controller 都通过本类拿到 uid / role，属于鉴权链路的最末端。
 * </p>
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    // ==================== TODO A-1 从请求头解析用户身份 ====================
    // 需求：
    //   1. parse：入参是完整请求头（形如 "Bearer xxx"），取出令牌后校验签名与有效期，返回 Claims
    //   2. getUid / getRole：从 Claims 中取出 uid、role
    //   3. 签名密钥必须与签发端（A-2）一致，且不得硬编码在源码中
    // 手册：§4.1 无状态认证的代价
    // 自测：篡改令牌任意一位 → 解析失败；过期令牌 → 解析失败；正常令牌 → 能取到 uid
    private SecretKey key;
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims parse(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            throw new BusinessException("登录过期");
        }
        System.out.println("[" + authHeader + "] len=" + authHeader.length());
        String token = authHeader.substring(7);
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    public Integer getUid(String authHeader) {
        Claims claims = parse(authHeader);
        return claims.get("uid", Integer.class);
    }

    public String getRole(String authHeader) {
        Claims claims = parse(authHeader);
        return claims.get("role",String.class);
    }

}
