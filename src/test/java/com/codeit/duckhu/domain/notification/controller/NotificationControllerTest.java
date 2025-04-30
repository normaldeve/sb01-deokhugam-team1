package com.codeit.duckhu.domain.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.duckhu.domain.notification.dto.CursorPageResponseNotificationDto;
import com.codeit.duckhu.domain.notification.dto.NotificationDto;
import com.codeit.duckhu.domain.notification.dto.NotificationUpdateRequest;
import com.codeit.duckhu.domain.notification.service.NotificationService;
import com.codeit.duckhu.domain.user.UserAuthenticationFilter;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.domain.user.repository.UserRepository;
import com.codeit.duckhu.global.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
@WebMvcTest(controllers = NotificationController.class)
@Import({GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    // 인증 필터
    @MockitoBean
    private UserRepository userRepository;

    @Nested
    @DisplayName("GET /api/notifications")
    class GetNotifications {

        @Test
        @DisplayName("성공 – 알림 페이지 응답")
        void getNotifications_success() throws Exception {
            // ── given ─────────────────────────────────────────────
            UUID userId = UUID.randomUUID();
            Instant now = Instant.now();

            // 1) 필터를 통과시키기 위해 userRepository.findById(userId) stub
            User mockUser =
                new User("a@b.com", "test", "pw");
            // 엔티티에 ID 세팅이 필요하다면 리플렉션 등으로 setId() 해 주시고,
            // 혹은 생성자에서 ID를 받도록 바꿔주세요.
            ReflectionTestUtils.setField(mockUser, "id", userId);

            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

            // 서비스 레이어 stub
            NotificationDto dto1 =
                new NotificationDto(
                    UUID.randomUUID(),
                    userId,
                    UUID.randomUUID(),
                    "리뷰 A",
                    "Alice님이 좋아합니다.",
                    false,
                    now.minusSeconds(60),
                    now.minusSeconds(60));
            NotificationDto dto2 =
                new NotificationDto(
                    UUID.randomUUID(),
                    userId,
                    UUID.randomUUID(),
                    "리뷰 B",
                    "Bob님이 댓글을 남겼습니다.",
                    false,
                    now.minusSeconds(30),
                    now.minusSeconds(30));
            List<NotificationDto> content = List.of(dto1, dto2);
            CursorPageResponseNotificationDto page =
                new CursorPageResponseNotificationDto(
                    content, null, null, content.size(), content.size(), false);
            given(notificationService.getNotifications(eq(userId), eq("DESC"), any(Instant.class),
                eq(2)))
                .willReturn(page);

            // when, then
            mockMvc
                .perform(
                    get("/api/notifications")
                        .param("userId", userId.toString())
                        .param("direction", "DESC")
                        .param("cursor", now.toString())
                        .param("limit", "2")
                        .requestAttr("authenticatedUser", mockUser)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].content").value(dto1.content()))
                .andExpect(jsonPath("$.hasNext").value(false));

            then(notificationService)
                .should()
                .getNotifications(eq(userId), eq("DESC"), any(Instant.class), eq(2));
        }

        @Test
        @DisplayName("실패 – 인증되지 않은 사용자는 403")
        void getNotifications_unauthenticated() throws Exception {
            mockMvc
                .perform(get("/api/notifications").param("userId", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/notifications/{notificationId}")
    class UpdateConfirmedStatus {

        @Test
        @DisplayName("성공 – 단일 알림 읽음 처리")
        void updateConfirmedStatus_success() throws Exception {
            UUID userId = UUID.randomUUID();
            UUID notificationId = UUID.randomUUID();
            Instant now = Instant.now();

            // mockUser 준비
            User mockUser = new User("a@b.com", "nick", "pw");
            ReflectionTestUtils.setField(mockUser, "id", userId);

            // 2) 서비스 레이어 stub
            NotificationDto updatedDto =
                new NotificationDto(
                    notificationId,
                    userId,
                    UUID.randomUUID(),
                    "리뷰 X",
                    "[Tester]님이 좋아합니다.",
                    true,
                    now.minusSeconds(5),
                    now);
            given(notificationService.updateConfirmedStatus(notificationId, userId, true))
                .willReturn(updatedDto);

            // 3) 수행 & 검증
            NotificationUpdateRequest req = new NotificationUpdateRequest(true);
            mockMvc
                .perform(
                    patch("/api/notifications/{id}", notificationId)
                        .requestAttr("authenticatedUser", mockUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notificationId.toString()))
                .andExpect(jsonPath("$.confirmed").value(true));

            then(notificationService).should().updateConfirmedStatus(notificationId, userId, true);
        }

        @Test
        @DisplayName("실패 – 인증되지 않으면 403")
        void updateConfirmedStatus_unauthenticated() throws Exception {
            mockMvc
                .perform(
                    patch("/api/notifications/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmed\":true}"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 – 잘못된 바디 형식으로 500")
        void updateConfirmedStatus_badRequest() throws Exception {
            UUID userId = UUID.randomUUID();

            // mockUser 준비 (인증 통과용)
            User mockUser = new User("a@b.com", "nick", "pw");
            ReflectionTestUtils.setField(mockUser, "id", userId);

            // 500 Internal ServerError
            mockMvc
                .perform(
                    patch("/api/notifications/{id}", UUID.randomUUID())
                        .requestAttr("authenticatedUser", mockUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"confirmed\":\"notABoolean\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/notifications/read-all")
    class UpdateAllConfirmedStatus {

        @Test
        @DisplayName("성공 – 모든 알림 일괄 읽음 처리")
        void updateAllConfirmedStatus_success() throws Exception {
            UUID userId = UUID.randomUUID();
            // mockUser 준비
            User mockUser = new User("a@b.com", "nick", "pw");
            ReflectionTestUtils.setField(mockUser, "id", userId);
            willDoNothing().given(notificationService).updateAllConfirmedStatus(userId);

            mockMvc
                .perform(
                    patch("/api/notifications/read-all").requestAttr("authenticatedUser", mockUser))
                .andExpect(status().isNoContent());

            then(notificationService).should().updateAllConfirmedStatus(userId);
        }

        @Test
        @DisplayName("실패 – 인증되지 않으면 403")
        void updateAllConfirmedStatus_unauthenticated() throws Exception {
            mockMvc.perform(patch("/api/notifications/read-all"))
                .andExpect(status().isForbidden());
        }
    }
}