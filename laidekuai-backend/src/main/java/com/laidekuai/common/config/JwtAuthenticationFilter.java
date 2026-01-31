package com.laidekuai.common.config;

import com.laidekuai.common.dto.ErrorCode;
import com.laidekuai.common.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 *
 * @author Laidekuai Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 从请求头获取token
        String token = extractToken(request);

        if (token != null) {
            try {
                var claims = jwtUtil.parseClaims(token);
                if (claims.getExpiration().before(new java.util.Date())) {
                    writeError(response, ErrorCode.TOKEN_EXPIRED);
                    return;
                }

                String role = claims.get("role", String.class);
                Long userId = claims.get("userId", Long.class);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT认证成功，用户ID: {}, 角色: {}", userId, role);
            } catch (ExpiredJwtException e) {
                writeError(response, ErrorCode.TOKEN_EXPIRED);
                return;
            } catch (JwtException e) {
                writeError(response, ErrorCode.TOKEN_INVALID);
                return;
            } catch (Exception e) {
                log.error("JWT解析失败", e);
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, ErrorCode code) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String body = String.format(
                "{\"code\":%d,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
                code.getCode(), code.getMessage(), System.currentTimeMillis());
        response.getWriter().write(body);
        response.getWriter().flush();
    }

    /**
     * 从请求头提取token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
