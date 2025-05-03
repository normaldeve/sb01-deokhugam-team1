package com.codeit.duckhu.domain.user.entity;

import com.codeit.duckhu.domain.comment.domain.Comment;
import com.codeit.duckhu.domain.review.entity.Review;
import com.codeit.duckhu.domain.user.dto.UserUpdateRequest;
import com.codeit.duckhu.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String nickname;

  @Column(nullable = false)
  private String password;

  @Column(name = "is_deleted", nullable = false)
  private boolean deleted;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Review> review = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PowerUser> powerUsers = new ArrayList<>();

  @Builder
  public User(String email, String nickname, String password) {
    this.email = email;
    this.nickname = nickname;
    this.password = password;
    this.deleted = false;
  }

  public void update(UserUpdateRequest userUpdateRequest) {
    if (!this.getNickname().equals(userUpdateRequest.getNickname())) {
      this.nickname = userUpdateRequest.getNickname();
    }
  }

  public void softDelete() {
    if (!this.isDeleted()) {
      this.deleted = true;
    }
  }
}
