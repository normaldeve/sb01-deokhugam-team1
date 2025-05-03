package com.codeit.duckhu.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain chain) throws ServletException, IOException{
        try {
            // 요청별 고유 ID
            String requestId = UUID.randomUUID().toString();
            MDC.put("requestId", requestId);

            // 클라이언트 IP
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            } else {
                // X-Forwarded-For에는 여러 IP가 콤마로 구분되어 있을 수 있으므로 첫 번째 IP 사용
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
            }
            MDC.put("ipAddress", ip);

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
