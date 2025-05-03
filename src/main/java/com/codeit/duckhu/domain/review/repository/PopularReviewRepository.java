package com.codeit.duckhu.domain.review.repository;

import com.codeit.duckhu.domain.review.entity.PopularReview;
import com.codeit.duckhu.domain.review.repository.custom.PopularReviewRepositoryCustom;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PopularReviewRepository
    extends JpaRepository<PopularReview, UUID>, PopularReviewRepositoryCustom {}
