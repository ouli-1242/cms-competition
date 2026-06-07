package com.cms.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security 工具类
 */
public class SecurityUtil {

    private SecurityUtil() {}

    /** 获取当前登录用户 ID */
    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        try {
            return Long.valueOf(auth.getPrincipal().toString());
        } catch (Exception e) {
            return null;
        }
    }

    /** 获取当前角色 */
    public static String currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return null;
        return auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse(null);
    }

    public static boolean isAdmin()   { return "ADMIN".equals(currentRole()); }
    public static boolean isTeacher() { return "TEACHER".equals(currentRole()); }
    public static boolean isStudent() { return "STUDENT".equals(currentRole()); }
}
