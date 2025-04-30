package com.codeit.duckhu.domain.comments;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.duckhu.domain.comment.controller.CommentController;
import com.codeit.duckhu.domain.comment.dto.CommentDto;
import com.codeit.duckhu.domain.comment.dto.request.CommentCreateRequest;
import com.codeit.duckhu.domain.comment.dto.request.CommentUpdateRequest;
import com.codeit.duckhu.domain.comment.service.CommentService;
import com.codeit.duckhu.domain.user.UserAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = CommentController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = UserAuthenticationFilter.class))
public class CommentControllerTest {
  @Autowired MockMvc mockMvc;

  @Autowired ObjectMapper objectMapper;

  @MockitoBean CommentService commentService;

  @Test
  void getMapping() throws Exception {
    UUID commentId = UUID.randomUUID();
    given(commentService.get(commentId)).willReturn(new CommentDto());

    mockMvc.perform(get("/api/comments/" + commentId)).andExpect(status().isOk());
  }

  @Test
  void postMapping() throws Exception {
    CommentCreateRequest request = new CommentCreateRequest();
    request.setContent("create comment");

    given(commentService.create(request)).willReturn(new CommentDto());

    mockMvc
        .perform(
            post("/api/comments")
                .header("Deokhugam-Request-User-ID", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  @Test
  void deleteMapping() throws Exception {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    doNothing().when(commentService).delete(commentId, userId);

    mockMvc
        .perform(delete("/api/comments/" + commentId).header("Deokhugam-Request-User-ID", userId))
        .andExpect(status().isNoContent());
  }

  @Test
  void updateMapping() throws Exception {
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    CommentUpdateRequest request = new CommentUpdateRequest();
    request.setContent("update comment");

    given(commentService.update(commentId, request, userId)).willReturn(new CommentDto());

    mockMvc
        .perform(
            patch("/api/comments/" + commentId)
                .header("Deokhugam-Request-User-ID", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }
}
