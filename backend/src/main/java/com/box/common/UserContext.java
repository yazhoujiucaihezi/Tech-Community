package com.box.common;

import com.box.entity.CurrentUser;

/**
 * 请求级上下文：一次请求内共享当前登录用户。
 * <p>
 * 由 LoginInterceptor 在 preHandle 中写入、afterCompletion 中清空，
 * Controller 直接读取，不再逐层传 uid / role。
 */
public class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    /** 存入当前用户，只由拦截器调用 */
    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    /** 取当前用户，未登录（白名单接口）返回 null */
    public static CurrentUser get() {
        return HOLDER.get();
    }

    /** 取用户 ID，未登录返回 null */
    public static Integer getUid() {
        CurrentUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    /** 取角色，未登录返回 null */
    public static String getRole() {
        CurrentUser user = HOLDER.get();
        return user == null ? null : user.getRole();
    }

    /** 清空，必须在请求结束时调用，否则线程复用会串号 */
    public static void clear() {
        HOLDER.remove();
    }
}
