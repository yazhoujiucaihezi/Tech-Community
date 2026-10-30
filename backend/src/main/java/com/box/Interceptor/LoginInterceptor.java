package com.box.Interceptor;

import com.box.common.UserContext;
import com.box.constant.RedisConstant;
import com.box.entity.CurrentUser;
import com.box.exception.BusinessException;
import com.box.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        String authorization = request.getHeader("Authorization");
        try {
            Claims claims = jwtUtils.parse(authorization);
            CurrentUser currentUser = new CurrentUser(claims.get("uid", Integer.class), claims.get("role", String.class));
            if (stringRedisTemplate.hasKey(RedisConstant.blackList+claims.getId())){
                throw new BusinessException("登录已过期");
            }
            UserContext.set(currentUser);
        }
        catch (Exception e){
            throw new BusinessException("登录已过期");
        }
        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, @Nullable Exception ex) {
        UserContext.clear();
    }
}
