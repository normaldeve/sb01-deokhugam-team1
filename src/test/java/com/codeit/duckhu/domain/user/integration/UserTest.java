package com.codeit.duckhu.domain.user.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.codeit.duckhu.domain.review.repository.TestJpaConfig;
import com.codeit.duckhu.domain.user.dto.CursorPageResponsePowerUserDto;
import com.codeit.duckhu.domain.user.dto.PowerUserDto;
import com.codeit.duckhu.domain.user.dto.UserDto;
import com.codeit.duckhu.domain.user.dto.UserLoginRequest;
import com.codeit.duckhu.domain.user.dto.UserRegisterRequest;
import com.codeit.duckhu.domain.user.dto.UserUpdateRequest;
import com.codeit.duckhu.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestJpaConfig.class)
public class UserTest {

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;

  private HttpHeaders getSessionHeader() throws Exception {
    UserLoginRequest loginRequest = new UserLoginRequest("test@example.com", "test1234!");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(loginRequest), headers);
    ResponseEntity<UserDto> response = restTemplate.postForEntity("/api/users/login", entity, UserDto.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    HttpHeaders responseHeaders = response.getHeaders();
    String cookie = responseHeaders.getFirst(HttpHeaders.SET_COOKIE);
    HttpHeaders sessionHeaders = new HttpHeaders();
    sessionHeaders.set(HttpHeaders.COOKIE, cookie);
    return sessionHeaders;
  }

  @Test
  @DisplayName("사용자 생성-성공")
  @Transactional
  void create_success() throws Exception {
    // given
    UserRegisterRequest request =
        new UserRegisterRequest("testA@example.com", "testA", "testa1234!");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    String json = objectMapper.writeValueAsString(request);
    HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);

    // when
    ResponseEntity<UserDto> response =
        restTemplate.postForEntity("/api/users", requestEntity, UserDto.class);

    // then
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody().getId());
    assertEquals("testA", response.getBody().getNickname());
    assertEquals("testA@example.com", response.getBody().getEmail());
  }

  @Test
  @DisplayName("로그인 - 요청헤더 인증 성공")
  @Sql("/data.sql")
  void login_success() throws Exception {
    // given
    UserLoginRequest request = new UserLoginRequest("test@example.com", "test1234!");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String json = objectMapper.writeValueAsString(request);
    HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);

    // when
    ResponseEntity<UserDto> response =
        restTemplate.postForEntity("/api/users/login", requestEntity, UserDto.class);
    // then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("테스트유저", response.getBody().getNickname());
    assertEquals("test@example.com", response.getBody().getEmail());
  }

  @Test
  @DisplayName("사용자 상세 조회- 성공")
  @Sql("/data.sql")
  void find_success()throws Exception {
    // given
    HttpHeaders sessionHeader = getSessionHeader();
    UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    HttpEntity<Void> requestEntity = new HttpEntity<>(sessionHeader);
    // when
    ResponseEntity<UserDto> response =
        restTemplate.exchange("/api/users/" + id, HttpMethod.GET, requestEntity, UserDto.class);

    // then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("테스트유저", response.getBody().getNickname());
    assertEquals("test@example.com", response.getBody().getEmail());
  }

  @Test
  @DisplayName("사용자 수정 - 성공")
  @Sql("/data.sql")
  void update_success() throws Exception {
    // given
    HttpHeaders sessionHeader = getSessionHeader();
    sessionHeader.setContentType(MediaType.APPLICATION_JSON);
    sessionHeader.setAccept(List.of(MediaType.APPLICATION_JSON));
    UUID targetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UserUpdateRequest request =new UserUpdateRequest("newName");

    sessionHeader.set("Deokhugam-Request-User-ID", targetId.toString());
    HttpEntity<String> httpEntity =
        new HttpEntity<>(objectMapper.writeValueAsString(request), sessionHeader);

    // when
    ResponseEntity<UserDto> response =
        restTemplate.exchange(
            "/api/users/" + targetId, HttpMethod.PATCH, httpEntity, UserDto.class);

    // then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("newName", response.getBody().getNickname());
  }

  @Test
  @DisplayName("사용자 논리삭제 - 성공")
  @Sql("/data.sql")
  void softDelete_success() throws Exception {
    // given
    HttpHeaders sessionHeader = getSessionHeader();
    UUID targetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    sessionHeader.set("Deokhugam-Request-User-ID", targetId.toString());
    HttpEntity<String> httpEntity = new HttpEntity<>(sessionHeader);

    // when
    ResponseEntity<Void> response =
        restTemplate.exchange("/api/users/" + targetId, HttpMethod.DELETE, httpEntity, Void.class);

    // then
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  @DisplayName("사용자 물리삭제 - 성공")
  @Sql("/data.sql")
  void hardDelete_success() throws Exception {
    // given
    HttpHeaders sessionHeader = getSessionHeader();
    UUID targetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    sessionHeader.set("Deokhugam-Request-User-ID", targetId.toString());
    HttpEntity<String> httpEntity = new HttpEntity<>(sessionHeader);

    // when
    ResponseEntity<Void> response =
        restTemplate.exchange(
            "/api/users/" + targetId + "/hard", HttpMethod.DELETE, httpEntity, Void.class);

    // then
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    boolean exists = userRepository.existsById(targetId);
    assertFalse(exists);
  }

  @Test
  @DisplayName("파워유저 조회 - 성공")
  @Sql("/data.sql")
  void findPowerUser_success() {
    //given
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/api/users/power")
            .queryParam("period", "MONTHLY")
            .queryParam("direction", "ASC")
            .queryParam("limit", 10);

    HttpEntity<?> entity = new HttpEntity<>(headers);

    //when
    ResponseEntity<CursorPageResponsePowerUserDto> response = restTemplate.exchange(uriBuilder.toUriString(),
            HttpMethod.GET,
            entity,
            CursorPageResponsePowerUserDto.class);

    //then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().getSize());

    PowerUserDto dto = response.getBody().getContent().get(0);
    assertEquals("테스트유저", dto.getNickname());
    assertEquals("MONTHLY", dto.getPeriod());
    assertEquals(1, dto.getRank());
    assertEquals(10.5, dto.getScore());
  }
}
