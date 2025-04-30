package com.codeit.duckhu.domain.user;

import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.domain.user.exception.UserException;
import com.codeit.duckhu.domain.user.repository.UserRepository;
import com.codeit.duckhu.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class UserAuthenticationFilter extends OncePerRequestFilter {
  private final UserRepository userRepository;

  public UserAuthenticationFilter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String path = request.getRequestURI();
    if (isPublicPath(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
        HttpSession session = request.getSession(false);
        if (session == null) {
          log.warn("세션 없음 - 인증 실패");
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          return;
        }

        UUID userId = (UUID) session.getAttribute("userId");
        if (userId == null) {
          log.warn("세션에 userId 없음 - 인증 실패");
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          return;
        }
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new UserException(ErrorCode.NOT_FOUND_USER)); // DB에서 사용자 조회
        request.setAttribute("authenticatedUser", user); // 사용자 정보 저장
      log.info("권한설정 작용 : 사용자 ID = {}", user.getId());
      } catch (Exception e) {
        log.warn("세션 인증 오류: {}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
    filterChain.doFilter(request, response);
  }

  private boolean isPublicPath(String path) {
    return path.equals("/")
            || path.equals("/api/users/login")
            || path.equals("/api/books/popular")
            || path.equals("/api/reviews/popular")
            || path.equals("/api/users")
            || path.equals("/api/users/power")
            || path.startsWith("/static")
            || path.startsWith("/favicon.ico")
            || path.startsWith("/index.html")
            || path.endsWith(".js")
            || path.endsWith(".css")
            || path.endsWith(".png")
            || path.endsWith(".jpg")
            || path.endsWith(".jpeg")
            || path.endsWith(".svg")
            || path.endsWith(".ico")
            || path.endsWith(".map")
            || path.endsWith(".html");
  }
}
