package com.codeit.duckhu.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserRegisterRequest {
  @Email(message = "유효한 이메일 주소를 입력해주세요")
  private final String email;

  @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해주세요.")
  @NotBlank(message = "닉네임은 최소 2자 이상이어야 합니다")
  private final String nickname;

  @NotBlank(message = "비밀번호는 최소 8자 이상이어야 합니다")
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
      message = "비밀번호는 8자 이상 20자 이하, 영문자, 숫자, 특수문자를 포함해야 합니다.")
  private final String password;
}
