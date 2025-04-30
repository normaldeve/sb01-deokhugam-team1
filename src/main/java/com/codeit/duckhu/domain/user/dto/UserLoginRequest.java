package com.codeit.duckhu.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserLoginRequest {

  @NotBlank(message = "유효한 이메일 주소를 입력해주세요")
  @Email(message = "유효한 이메일 주소를 입력해주세요")
  private final String email;

  @NotBlank(message = "비밀번호를 입력해주세요")
  private final String password;
}
