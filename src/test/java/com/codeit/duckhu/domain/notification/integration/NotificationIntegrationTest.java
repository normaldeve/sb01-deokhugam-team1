package com.codeit.duckhu.domain.notification.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/notifications.sql")
class NotificationIntegrationTest {

    private static final UUID RECEIVER_ID    = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID REVIEW_ID      = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID NOTIFICATION_ID= UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession session;

    @BeforeEach
    void setUpSession() {
        session = new MockHttpSession();
        // SQL 에서 INSERT 한 receiver 유저의 ID
        session.setAttribute("userId", RECEIVER_ID);
    }

    @Test
    @DisplayName("GET /api/notifications – 인증된 사용자는 내 알림 조회")
    void listNotifications_success() throws Exception {
        mockMvc.perform(get("/api/notifications")
                .session(session)
                .param("userId", RECEIVER_ID.toString())
                .param("limit", "10")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(NOTIFICATION_ID.toString()))
            .andExpect(jsonPath("$.content[0].reviewId").value(REVIEW_ID.toString()))
            .andExpect(jsonPath("$.content[0].confirmed").value(false))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("GET /api/notifications – 인증 없으면 401")
    void listNotifications_unauthorized() throws Exception {
        mockMvc.perform(get("/api/notifications")
                .param("userId", RECEIVER_ID.toString())
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id} – 단일 알림 읽음 처리")
    void markOneAsRead_success() throws Exception {
        var payload = objectMapper.writeValueAsString(
            new com.codeit.duckhu.domain.notification.dto.NotificationUpdateRequest(true)
        );

        mockMvc.perform(patch("/api/notifications/{id}", NOTIFICATION_ID)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(NOTIFICATION_ID.toString()))
            .andExpect(jsonPath("$.confirmed").value(true));
    }

    @Test
    @DisplayName("PATCH /api/notifications/read-all – 모든 알림 일괄 읽음 처리")
    void markAllAsRead_success() throws Exception {
        mockMvc.perform(patch("/api/notifications/read-all")
                .session(session)
            )
            .andExpect(status().isNoContent());

        // 처리 후 조회하면 confirmed=true 로 바뀌었는지 확인
        mockMvc.perform(get("/api/notifications")
                .session(session)
                .param("userId", RECEIVER_ID.toString())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].confirmed").value(true));
    }
}
