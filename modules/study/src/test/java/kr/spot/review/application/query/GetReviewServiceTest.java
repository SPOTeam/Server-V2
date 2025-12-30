package kr.spot.review.application.query;

import static kr.spot.review.common.ReviewFixture.ACTIVITY;
import static kr.spot.review.common.ReviewFixture.ENCOURAGEMENT;
import static kr.spot.review.common.ReviewFixture.LEARNED;
import static kr.spot.review.common.ReviewFixture.MEMBER_ID;
import static kr.spot.review.common.ReviewFixture.OTHER_MEMBER_ID;
import static kr.spot.review.common.ReviewFixture.STUDY_ID;
import static kr.spot.review.common.ReviewFixture.privateReview;
import static kr.spot.review.common.ReviewFixture.review;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.spot.review.domain.Review;
import kr.spot.review.domain.enums.Reaction;
import kr.spot.review.infrastructure.jpa.querydsl.ReviewQueryRepository;
import kr.spot.review.infrastructure.jpa.querydsl.ReviewQueryRepository.ReactionCounts;
import kr.spot.review.presentation.query.dto.GetReviewListResponse;
import kr.spot.review.presentation.query.dto.GetReviewListResponse.ReviewResponse;
import kr.spot.study.application.validator.StudyAccessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetReviewServiceTest {

  private static final String PRIVATE_CONTENT_MESSAGE = "이 글은 스터디원에게만 노출됩니다.";

  @Mock
  ReviewQueryRepository reviewQueryRepository;

  @Mock
  StudyAccessValidator studyAccessValidator;

  GetReviewService getReviewService;

  @BeforeEach
  void setUp() {
    getReviewService = new GetReviewService(reviewQueryRepository, studyAccessValidator);
  }

  @Nested
  @DisplayName("회고 목록 조회 (getReviewList)")
  class GetReviewList {

    @Test
    @DisplayName("회고 목록을 정상적으로 조회할 수 있다")
    void should_get_review_list_successfully() {
      // given
      Review review1 = review(1L);
      Review review2 = review(2L);

      when(studyAccessValidator.isStudyMember(anyLong(), anyLong())).thenReturn(true);
      when(reviewQueryRepository.findByStudyIdWithCursor(anyLong(), any(), anyInt()))
          .thenReturn(List.of(review1, review2));
      when(reviewQueryRepository.findReactionCountsByReviewIds(any())).thenReturn(Map.of());
      when(reviewQueryRepository.findMemberReactionsByReviewIds(any(), any())).thenReturn(Map.of());
      when(reviewQueryRepository.countByStudyId(anyLong())).thenReturn(2L);

      // when
      GetReviewListResponse response = getReviewService.getReviewList(STUDY_ID, MEMBER_ID, null,
          10);

      // then
      assertThat(response.reviews()).hasSize(2);
      assertThat(response.hasNext()).isFalse();
      assertThat(response.nextCursor()).isNull();
      assertThat(response.totalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("페이지네이션이 정상적으로 동작한다")
    void should_paginate_correctly() {
      // given
      Review review1 = review(3L);
      Review review2 = review(2L);
      Review review3 = review(1L);

      when(studyAccessValidator.isStudyMember(anyLong(), anyLong())).thenReturn(true);
      when(reviewQueryRepository.findByStudyIdWithCursor(anyLong(), any(), anyInt()))
          .thenReturn(List.of(review1, review2, review3));
      when(reviewQueryRepository.findReactionCountsByReviewIds(any())).thenReturn(Map.of());
      when(reviewQueryRepository.findMemberReactionsByReviewIds(any(), any())).thenReturn(Map.of());
      when(reviewQueryRepository.countByStudyId(anyLong())).thenReturn(5L);

      // when
      GetReviewListResponse response = getReviewService.getReviewList(STUDY_ID, MEMBER_ID, null, 2);

      // then
      assertThat(response.reviews()).hasSize(2);
      assertThat(response.hasNext()).isTrue();
      assertThat(response.nextCursor()).isEqualTo(2L);
    }

    @Test
    @DisplayName("회고가 없는 경우 빈 목록을 반환한다")
    void should_return_empty_list_when_no_reviews() {
      // given
      when(studyAccessValidator.isStudyMember(anyLong(), anyLong())).thenReturn(true);
      when(reviewQueryRepository.findByStudyIdWithCursor(anyLong(), any(), anyInt()))
          .thenReturn(Collections.emptyList());
      when(reviewQueryRepository.findReactionCountsByReviewIds(any())).thenReturn(Map.of());
      when(reviewQueryRepository.findMemberReactionsByReviewIds(any(), any())).thenReturn(Map.of());
      when(reviewQueryRepository.countByStudyId(anyLong())).thenReturn(0L);

      // when
      GetReviewListResponse response = getReviewService.getReviewList(STUDY_ID, MEMBER_ID, null,
          10);

      // then
      assertThat(response.reviews()).isEmpty();
      assertThat(response.hasNext()).isFalse();
      assertThat(response.totalElements()).isZero();
    }

    @Test
    @DisplayName("반응 카운트가 올바르게 조회된다")
    void should_get_reaction_counts_correctly() {
      // given
      Review review1 = review(1L);
      ReactionCounts counts = new ReactionCounts(5, 3, 2, 1);

      when(studyAccessValidator.isStudyMember(anyLong(), anyLong())).thenReturn(true);
      when(reviewQueryRepository.findByStudyIdWithCursor(anyLong(), any(), anyInt()))
          .thenReturn(List.of(review1));
      when(reviewQueryRepository.findReactionCountsByReviewIds(any()))
          .thenReturn(Map.of(1L, counts));
      when(reviewQueryRepository.findMemberReactionsByReviewIds(any(), any())).thenReturn(Map.of());
      when(reviewQueryRepository.countByStudyId(anyLong())).thenReturn(1L);

      // when
      GetReviewListResponse response = getReviewService.getReviewList(STUDY_ID, MEMBER_ID, null,
          10);

      // then
      ReviewResponse reviewResponse = response.reviews().getFirst();
      assertThat(reviewResponse.reactionCounts().fireCount()).isEqualTo(5L);
      assertThat(reviewResponse.reactionCounts().heartCount()).isEqualTo(3L);
      assertThat(reviewResponse.reactionCounts().starCount()).isEqualTo(2L);
      assertThat(reviewResponse.reactionCounts().smileCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("사용자의 반응 여부가 올바르게 조회된다")
    void should_get_member_reactions_correctly() {
      // given
      Review review1 = review(1L);

      when(studyAccessValidator.isStudyMember(anyLong(), anyLong())).thenReturn(true);
      when(reviewQueryRepository.findByStudyIdWithCursor(anyLong(), any(), anyInt()))
          .thenReturn(List.of(review1));
      when(reviewQueryRepository.findReactionCountsByReviewIds(any())).thenReturn(Map.of());
      when(reviewQueryRepository.findMemberReactionsByReviewIds(any(), any()))
          .thenReturn(Map.of(1L, Set.of(Reaction.FIRE, Reaction.HEART)));
      when(reviewQueryRepository.countByStudyId(anyLong())).thenReturn(1L);

      // when
      GetReviewListResponse response = getReviewService.getReviewList(STUDY_ID, MEMBER_ID, null,
          10);

      // then
      ReviewResponse reviewResponse = response.reviews().getFirst();
      assertThat(reviewResponse.reactions().isFired()).isTrue();
      assertThat(reviewResponse.reactions().isHearted()).isTrue();
      assertThat(reviewResponse.reactions().isStarred()).isFalse();
      assertThat(reviewResponse.reactions().isSmiled()).isFalse();
    }
  }

  @Nested
  @DisplayName("비공개 회고 마스킹")
  class PrivateReviewMasking {

    @Test
    @DisplayName("스터디원이 비공개 회고를 조회하면 원본 내용이 보인다")
    void should_show_original_content_for_study_member() {
      // given
      Review privateReview = privateReview(1L, STUDY_ID, MEMBER_ID);

      when(studyAccessValidator.isStudyMember(anyLong(), anyLong())).thenReturn(true);
      when(reviewQueryRepository.findByStudyIdWithCursor(anyLong(), any(), anyInt()))
          .thenReturn(List.of(privateReview));
      when(reviewQueryRepository.findReactionCountsByReviewIds(any())).thenReturn(Map.of());
      when(reviewQueryRepository.findMemberReactionsByReviewIds(any(), any())).thenReturn(Map.of());
      when(reviewQueryRepository.countByStudyId(anyLong())).thenReturn(1L);

      // when
      GetReviewListResponse response = getReviewService.getReviewList(STUDY_ID, MEMBER_ID, null,
          10);

      // then
      ReviewResponse reviewResponse = response.reviews().getFirst();
      assertThat(reviewResponse.isPrivate()).isTrue();
      assertThat(reviewResponse.content().activity()).isEqualTo(ACTIVITY);
      assertThat(reviewResponse.content().learned()).isEqualTo(LEARNED);
      assertThat(reviewResponse.content().encouragement()).isEqualTo(ENCOURAGEMENT);
    }

    @Test
    @DisplayName("스터디원이 아닌 사용자가 비공개 회고를 조회하면 마스킹된 내용이 보인다")
    void should_mask_content_for_non_study_member() {
      // given
      Review privateReview = privateReview(1L, STUDY_ID, MEMBER_ID);

      when(studyAccessValidator.isStudyMember(anyLong(), anyLong())).thenReturn(false);
      when(reviewQueryRepository.findByStudyIdWithCursor(anyLong(), any(), anyInt()))
          .thenReturn(List.of(privateReview));
      when(reviewQueryRepository.findReactionCountsByReviewIds(any())).thenReturn(Map.of());
      when(reviewQueryRepository.findMemberReactionsByReviewIds(any(), any())).thenReturn(Map.of());
      when(reviewQueryRepository.countByStudyId(anyLong())).thenReturn(1L);

      // when
      GetReviewListResponse response = getReviewService.getReviewList(STUDY_ID, OTHER_MEMBER_ID,
          null, 10);

      // then
      ReviewResponse reviewResponse = response.reviews().getFirst();
      assertThat(reviewResponse.isPrivate()).isTrue();
      assertThat(reviewResponse.content().activity()).isEqualTo(PRIVATE_CONTENT_MESSAGE);
      assertThat(reviewResponse.content().learned()).isEqualTo(PRIVATE_CONTENT_MESSAGE);
      assertThat(reviewResponse.content().encouragement()).isEqualTo(PRIVATE_CONTENT_MESSAGE);
      assertThat(reviewResponse.content().imageUrl()).isNull();
    }

    @Test
    @DisplayName("공개 회고는 스터디원이 아니어도 원본 내용이 보인다")
    void should_show_original_content_for_public_review() {
      // given
      Review publicReview = review(1L);

      when(studyAccessValidator.isStudyMember(anyLong(), anyLong())).thenReturn(false);
      when(reviewQueryRepository.findByStudyIdWithCursor(anyLong(), any(), anyInt()))
          .thenReturn(List.of(publicReview));
      when(reviewQueryRepository.findReactionCountsByReviewIds(any())).thenReturn(Map.of());
      when(reviewQueryRepository.findMemberReactionsByReviewIds(any(), any())).thenReturn(Map.of());
      when(reviewQueryRepository.countByStudyId(anyLong())).thenReturn(1L);

      // when
      GetReviewListResponse response = getReviewService.getReviewList(STUDY_ID, OTHER_MEMBER_ID,
          null, 10);

      // then
      ReviewResponse reviewResponse = response.reviews().getFirst();
      assertThat(reviewResponse.isPrivate()).isFalse();
      assertThat(reviewResponse.content().activity()).isEqualTo(ACTIVITY);
      assertThat(reviewResponse.content().learned()).isEqualTo(LEARNED);
      assertThat(reviewResponse.content().encouragement()).isEqualTo(ENCOURAGEMENT);
    }

    @Test
    @DisplayName("비로그인 사용자가 비공개 회고를 조회하면 마스킹된 내용이 보인다")
    void should_mask_content_for_anonymous_user() {
      // given
      Review privateReview = privateReview(1L, STUDY_ID, MEMBER_ID);

      when(reviewQueryRepository.findByStudyIdWithCursor(anyLong(), any(), anyInt()))
          .thenReturn(List.of(privateReview));
      when(reviewQueryRepository.findReactionCountsByReviewIds(any())).thenReturn(Map.of());
      when(reviewQueryRepository.findMemberReactionsByReviewIds(any(), any())).thenReturn(Map.of());
      when(reviewQueryRepository.countByStudyId(anyLong())).thenReturn(1L);

      // when (viewerId가 null인 경우)
      GetReviewListResponse response = getReviewService.getReviewList(STUDY_ID, null, null, 10);

      // then
      ReviewResponse reviewResponse = response.reviews().getFirst();
      assertThat(reviewResponse.content().activity()).isEqualTo(PRIVATE_CONTENT_MESSAGE);
      assertThat(reviewResponse.content().learned()).isEqualTo(PRIVATE_CONTENT_MESSAGE);
      assertThat(reviewResponse.content().encouragement()).isEqualTo(PRIVATE_CONTENT_MESSAGE);
    }
  }
}
