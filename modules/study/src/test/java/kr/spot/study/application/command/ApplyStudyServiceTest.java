package kr.spot.study.application.command;

import static kr.spot.study.common.StudyFixture.LEADER_ID;
import static kr.spot.study.common.StudyFixture.study;
import static kr.spot.study.common.StudyMemberFixture.MEMBER_ID;
import static kr.spot.study.common.StudyMemberFixture.STUDY_ID;
import static kr.spot.study.common.StudyMemberFixture.applied;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.IdGenerator;
import kr.spot.event.StudyApplicationProcessedEvent;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.Decision;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ApplyStudyServiceTest {

  @Mock
  IdGenerator idGenerator;

  @Mock
  ApplicationEventPublisher eventPublisher;

  @Mock
  StudyRepository studyRepository;

  @Mock
  StudyMemberRepository studyMemberRepository;

  @Captor
  ArgumentCaptor<StudyApplicationProcessedEvent> eventCaptor;

  ApplyStudyService applyStudyService;

  @BeforeEach
  void setUp() {
    applyStudyService = new ApplyStudyService(
        idGenerator, eventPublisher, studyRepository, studyMemberRepository);
  }

  @Nested
  @DisplayName("스터디 신청 처리 (processStudyApplication)")
  class ProcessStudyApplication {

    @Test
    @DisplayName("신청 승인 시 이벤트가 발행된다")
    void should_publish_event_when_application_approved() {
      // given
      Long applicationId = 1L;
      StudyMember application = applied(applicationId, STUDY_ID, MEMBER_ID, "참여하고 싶습니다");
      Study study = study();

      when(studyMemberRepository.getStudyMemberById(anyLong())).thenReturn(application);
      when(studyRepository.getStudyById(anyLong())).thenReturn(study);

      // when
      applyStudyService.processStudyApplication(applicationId, LEADER_ID, Decision.APPROVE);

      // then
      verify(eventPublisher).publishEvent(eventCaptor.capture());

      StudyApplicationProcessedEvent event = eventCaptor.getValue();
      assertThat(event.studyId()).isEqualTo(study.getId());
      assertThat(event.applicantId()).isEqualTo(MEMBER_ID);
      assertThat(event.processedBy()).isEqualTo(LEADER_ID);
      assertThat(event.decision()).isEqualTo("APPROVE");
      assertThat(event.studyName()).isEqualTo(study.getName());
      assertThat(event.isApproved()).isTrue();
    }

    @Test
    @DisplayName("신청 거절 시 이벤트가 발행된다")
    void should_publish_event_when_application_rejected() {
      // given
      Long applicationId = 1L;
      StudyMember application = applied(applicationId, STUDY_ID, MEMBER_ID, "참여하고 싶습니다");
      Study study = study();

      when(studyMemberRepository.getStudyMemberById(anyLong())).thenReturn(application);
      when(studyRepository.getStudyById(anyLong())).thenReturn(study);

      // when
      applyStudyService.processStudyApplication(applicationId, LEADER_ID, Decision.REJECT);

      // then
      verify(eventPublisher).publishEvent(eventCaptor.capture());

      StudyApplicationProcessedEvent event = eventCaptor.getValue();
      assertThat(event.decision()).isEqualTo("REJECT");
      assertThat(event.isApproved()).isFalse();
    }
  }
}
