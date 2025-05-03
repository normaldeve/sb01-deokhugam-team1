package com.codeit.duckhu.domain.user.service;

import com.codeit.duckhu.domain.user.dto.CursorPageResponsePowerUserDto;
import com.codeit.duckhu.domain.user.dto.PowerUserDto;
import com.codeit.duckhu.domain.user.dto.PowerUserStatsDto;
import com.codeit.duckhu.domain.user.dto.UserDto;
import com.codeit.duckhu.domain.user.dto.UserLoginRequest;
import com.codeit.duckhu.domain.user.dto.UserRegisterRequest;
import com.codeit.duckhu.domain.user.dto.UserUpdateRequest;
import com.codeit.duckhu.domain.user.entity.PowerUser;
import com.codeit.duckhu.domain.user.entity.User;
import com.codeit.duckhu.domain.user.exception.EmailDuplicateException;
import com.codeit.duckhu.domain.user.exception.UserException;
import com.codeit.duckhu.domain.user.mapper.PowerUserMapper;
import com.codeit.duckhu.domain.user.mapper.UserMapper;
import com.codeit.duckhu.domain.user.repository.UserRepository;
import com.codeit.duckhu.domain.user.repository.poweruser.PowerUserRepository;
import com.codeit.duckhu.global.exception.ErrorCode;
import com.codeit.duckhu.global.type.Direction;
import com.codeit.duckhu.global.type.PeriodType;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PowerUserRepository powerUserRepository;
  private final UserMapper userMapper;
  private final PowerUserMapper powerUserMapper;

  @Override
  public UserDto create(UserRegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new EmailDuplicateException(request.getEmail());
    }

    User user = new User(request.getEmail(), request.getNickname(), request.getPassword());

    User savedUser = userRepository.save(user);
    log.info("[사용자 등록 완료] id: {}, nickname : {}", savedUser.getId(), savedUser.getNickname());

    return userMapper.toDto(savedUser);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto login(UserLoginRequest userLoginRequest) {
    String email = userLoginRequest.getEmail();
    String password = userLoginRequest.getPassword();
    User user =
        userRepository
            .findByEmail(email)
            .orElseGet(
                () -> {
                  log.info("[로그인 실패] 존재하지 않는 이메일: {}", email);
                  throw new UserException(ErrorCode.LOGIN_INPUT_INVALID);
                });
    if (!user.getPassword().equals(password)) {
      log.info("[로그인 실패] 일치하지 않는 비밀번호: {}", password);
      throw new UserException(ErrorCode.LOGIN_INPUT_INVALID);
    }
    log.info("[로그인 완료] 사용자 ID: {}, 이메일: {}", user.getId(), email);
    return userMapper.toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto findById(UUID id) {
    User user =
        userRepository
            .findById(id)
            .orElseGet(
                () -> {
                  log.info("[사용자 조회 실패] id: {}", id);
                  throw new UserException(ErrorCode.NOT_FOUND_USER);
                });
    if (user.isDeleted()) {
      log.info("[사용자 조회 실패] 논리 삭제된 id: {}", id);
      throw new UserException(ErrorCode.NOT_FOUND_USER);
    }
    return userMapper.toDto(user);
  }

  @Override
  public UserDto update(UUID id, UserUpdateRequest userUpdateRequest) {
    User user =
        userRepository
            .findById(id)
            .orElseGet(
                () -> {
                  log.info("[사용자 조회 실패] id: {}", id);
                  throw new UserException(ErrorCode.NOT_FOUND_USER);
                });

    if (user.isDeleted()) {
      log.info("[사용자 조회 실패] 논리 삭제된 id: {}", id);
      throw new UserException(ErrorCode.NOT_FOUND_USER);
    }
    user.update(userUpdateRequest);
    return userMapper.toDto(user);
  }

  public User findByIdEntityReturn(UUID id) {
    User user =
        userRepository
            .findById(id)
            .orElseGet(
                () -> {
                  log.info("[사용자 조회 실패] id: {}", id);
                  throw new UserException(ErrorCode.NOT_FOUND_USER);
                });

    if (user.isDeleted()) {
      log.info("[사용자 조회 실패] 논리 삭제된 id: {}", id);
      throw new UserException(ErrorCode.NOT_FOUND_USER);
    }

    return user;
  }

  @Override
  public void softDelete(UUID id) {
    User user =
        userRepository
            .findById(id)
            .orElseGet(
                () -> {
                  log.info("[사용자 조회 실패] id: {}", id);
                  throw new UserException(ErrorCode.NOT_FOUND_USER);
                });

    user.softDelete();
    log.info("[사용자 논리 삭제 완료] id: {}", id);
  }

  @Override
  public void hardDelete(UUID id) {
    User user =
        userRepository
            .findById(id)
            .orElseGet(
                () -> {
                  log.info("[사용자 조회 실패] id: {}", id);
                  throw new UserException(ErrorCode.NOT_FOUND_USER);
                });

    userRepository.deleteById(user.getId());
    log.warn("[사용자 물리 삭제 완료] id: {}", id); // 정상적이지만 주의가 필요한 행동(복구 불가능한 행위)이니까 warn
  }

  @Override
  public void savePowerUser(PeriodType period) {
    try {
      Instant now = Instant.now();
      Instant start = period.toStartInstant(now);
      Instant end = now;

      log.info("[Batch 시작] period={} | from={} ~ to={}", period, start, now);

      // 계산에 필요한 요소들 갖고오기
      List<PowerUserStatsDto> stats = powerUserRepository.findPowerUserStatsBetween(start, end);

      // 유저목록가져오기
      Set<UUID> userIds = stats.stream().map(PowerUserStatsDto::userId).collect(Collectors.toSet());
      Map<UUID, User> userMap =
          userRepository.findAllById(userIds).stream()
              .collect(Collectors.toMap(User::getId, Function.identity()));

      // 활동점수 계산
      List<PowerUser> powerUsers =
          stats.stream()
              .map(
                  dto -> {
                    Double score =
                        dto.reviewScoreSum() * 0.5
                            + dto.likedCount() * 0.2
                            + dto.commentCount() * 0.3;
                    User user =
                        Optional.ofNullable(userMap.get(dto.userId()))
                            .orElseThrow(() -> new UserException(ErrorCode.NOT_FOUND_USER));

                    log.info(
                        "파워유저: {} | 활동 점수: {} | 리뷰 점수 합: {} | 좋아요 수: {} | 댓글 수: {}",
                        user.getNickname(),
                        score,
                        dto.reviewScoreSum(),
                        dto.likedCount(),
                        dto.commentCount());

                    return PowerUser.builder()
                        .user(user)
                        .reviewScoreSum(dto.reviewScoreSum())
                        .likeCount(dto.likedCount())
                        .commentCount(dto.commentCount())
                        .score(score)
                        .period(period)
                        .build();
                  })
              .sorted(Comparator.comparingDouble(PowerUser::getScore).reversed())
              .toList();

      // 순위 부여-동시성제어
      AtomicInteger rankCount = new AtomicInteger(1);
      // 순위 설정
      powerUsers.forEach(powerUser -> powerUser.setRank(rankCount.getAndIncrement()));

      // 기존 데이터 삭제(for 배치)
      powerUserRepository.deleteByPeriod(period);
      log.info("[삭제 완료] 기존 PowerUser 삭제 - period={}", period);

      // PowerUser에 저장
      powerUserRepository.saveAll(powerUsers);
      log.info("[PowerUser 저장 완료] 대상 수: {}, period={}", powerUsers.size(), period);
    } catch (Exception e) {
      log.warn(
          "[Batch 오류] period = {} 처리 중 오류 발생 : {}", period, e.getMessage()); // 배치작업 오류 그냥 넘어가면 안되니까
    }
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponsePowerUserDto findPowerUsers(
      PeriodType period, Direction direction, String cursor, Instant after, int limit) {

    List<PowerUser> powerUsers =
        powerUserRepository.searchByPeriodWithCursorPaging(
            period, direction, cursor, after, limit + 1);

    boolean hasNext = powerUsers.size() > limit;

    // 저장된 PowerUser 커서페이지로 갖고오기
    List<PowerUser> pageContent = hasNext ? powerUsers.subList(0, limit) : powerUsers;
    List<PowerUserDto> list = pageContent.stream().map(powerUserMapper::toDto).toList();

    String nextCursor = null;
    Instant nextAfter = null;
    if (hasNext && !pageContent.isEmpty()) {
      PowerUser last = pageContent.get(pageContent.size() - 1);
      nextCursor = last.getUser().getId().toString();
      nextAfter = last.getCreatedAt();
    }

    return CursorPageResponsePowerUserDto.builder()
        .content(list)
        .nextCursor(nextCursor)
        .nextAfter(nextAfter)
        .size(list.size())
        .totalElements(list.size())
        .hasNext(hasNext)
        .build();
  }
}
