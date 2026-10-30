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

    private SecretKey key;
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims parse(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            throw new BusinessException("登录过期");
        }
        String token = authHeader.substring(7);
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }
}
