package kr.spot.study.application.command;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import kr.spot.IdGenerator;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyLikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudyLikeService 단위 테스트")
class StudyLikeServiceTest {

  @Mock
  IdGenerator idGenerator;

  @Mock
  StudyLikeRepository studyLikeRepository;

  @Mock
  StudyRepository studyRepository;

  @InjectMocks
  StudyLikeService sut;

  @Nested
  @DisplayName("likeStudy 메서드는")
  class LikeStudyTest {

    private final long studyId = 1L;
    private final long memberId = 100L;
    private final long generatedId = 999L;

    @Test
    @DisplayName("새로운 좋아요인 경우 좋아요 수를 증가시킨다")
    void should_increase_like_count_when_new_like() {
      // given
      given(idGenerator.nextId()).willReturn(generatedId);
      given(studyLikeRepository.saveStudyLike(generatedId, studyId, memberId)).willReturn(1);

      // when
      sut.likeStudy(studyId, memberId);

      // then
      verify(studyLikeRepository).saveStudyLike(generatedId, studyId, memberId);
      verify(studyRepository).increaseLike(studyId);
    }

    @Test
    @DisplayName("중복 좋아요인 경우 좋아요 수를 증가시키지 않는다")
    void should_not_increase_like_count_when_duplicate_like() {
      // given
      given(idGenerator.nextId()).willReturn(generatedId);
      given(studyLikeRepository.saveStudyLike(generatedId, studyId, memberId)).willReturn(0);

      // when
      sut.likeStudy(studyId, memberId);

      // then
      verify(studyLikeRepository).saveStudyLike(generatedId, studyId, memberId);
      verify(studyRepository, never()).increaseLike(studyId);
    }
  }

  @Nested
  @DisplayName("unlikeStudy 메서드는")
  class UnlikeStudyTest {

    private final long studyId = 1L;
    private final long memberId = 100L;

    @Test
    @DisplayName("좋아요가 존재하면 삭제하고 좋아요 수를 감소시킨다")
    void should_decrease_like_count_when_like_exists() {
      // given
      given(studyLikeRepository.hardDelete(studyId, memberId)).willReturn(1);

      // when
      sut.unlikeStudy(studyId, memberId);

      // then
      verify(studyLikeRepository).hardDelete(studyId, memberId);
      verify(studyRepository).decreaseLike(studyId);
    }

    @Test
    @DisplayName("좋아요가 존재하지 않으면 좋아요 수를 감소시키지 않는다")
    void should_not_decrease_like_count_when_like_not_exists() {
      // given
      given(studyLikeRepository.hardDelete(studyId, memberId)).willReturn(0);

      // when
      sut.unlikeStudy(studyId, memberId);

      // then
      verify(studyLikeRepository).hardDelete(studyId, memberId);
      verify(studyRepository, never()).decreaseLike(studyId);
    }
  }
}
