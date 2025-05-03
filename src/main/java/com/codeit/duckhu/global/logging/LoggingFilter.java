package com.codeit.duckhu.global.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class LoggingFilter implements Filter {

  private static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    try {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      HttpServletResponse httpResponse = (HttpServletResponse) response;

      String userId = httpRequest.getHeader(USER_ID_HEADER);

      if (userId == null) {
        userId = "Anonymous";
      }

      String requestId = UUID.randomUUID().toString();

      MDC.put("userId", userId);
      MDC.put("requestId", requestId);

      // 응답 헤더에 요청 ID 추가
      httpResponse.setHeader("Deokhugam-Request-ID", requestId);

      chain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }
}
