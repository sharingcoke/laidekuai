package com.laidekuai.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 *
 * @author Laidekuai Team
 */
public class SecurityUtils {

    /**
     * 获取当前用户ID
     *
     * @return 用户ID，如果未登录返回null
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名，如果未登录返回null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * 判断当前用户是否已登录
     *
     * @return 是否已登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 判断当前用户是否是管理员
     *
     * @return 是否是管理员
     */
    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        }
        return false;
    }
}
