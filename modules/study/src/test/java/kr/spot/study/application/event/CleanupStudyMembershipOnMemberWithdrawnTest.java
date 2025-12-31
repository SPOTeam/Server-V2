package kr.spot.study.application.event;

import static org.mockito.Mockito.verify;

import kr.spot.domain.events.MemberWithdrawnEvent;
import kr.spot.study.infrastructure.jpa.associations.StudyLikeRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CleanupStudyMembershipOnMemberWithdrawnTest {

  private static final long MEMBER_ID = 1L;

  @Mock
  StudyMemberRepository studyMemberRepository;

  @Mock
  StudyLikeRepository studyLikeRepository;

  CleanupStudyMembershipOnMemberWithdrawn handler;

  @BeforeEach
  void setUp() {
    handler = new CleanupStudyMembershipOnMemberWithdrawn(studyMemberRepository, studyLikeRepository);
  }

  @Test
  @DisplayName("회원 탈퇴 시 스터디 멤버십과 좋아요를 삭제한다")
  void should_deleteStudyMembershipAndLikes_when_memberWithdrawn() {
    // given
    MemberWithdrawnEvent event = new MemberWithdrawnEvent(MEMBER_ID);

    // when
    handler.handle(event);

    // then
    verify(studyMemberRepository).deleteByMemberId(MEMBER_ID);
    verify(studyLikeRepository).deleteAllByMemberId(MEMBER_ID);
  }
}
