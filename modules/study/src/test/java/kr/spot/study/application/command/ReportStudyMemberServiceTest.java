package kr.spot.study.application.command;

import static kr.spot.study.common.StudyMemberFixture.MEMBER_ID;
import static kr.spot.study.common.StudyMemberFixture.STUDY_ID;
import static kr.spot.study.common.StudyMemberFixture.applied;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.IdGenerator;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.associations.StudyMemberReport;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberReportRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.study.presentation.command.dto.request.RepostStudyMemberRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportStudyMemberServiceTest {

  @Mock
  IdGenerator idGenerator;

  @Mock
  StudyMemberRepository studyMemberRepository;

  @Mock
  StudyMemberReportRepository studyMemberReportRepository;

  @Captor
  ArgumentCaptor<StudyMemberReport> reportCaptor;

  ReportStudyMemberService reportStudyMemberService;

  @BeforeEach
  void setUp() {
    reportStudyMemberService = new ReportStudyMemberService(
        idGenerator, studyMemberRepository, studyMemberReportRepository);
  }

  @Nested
  @DisplayName("스터디 멤버 신고 (reportStudyMember)")
  class ReportStudyMember {

    @Test
    @DisplayName("스터디 멤버를 신고하면 신고 정보가 저장된다")
    void should_save_report_when_reporting_study_member() {
      // given
      Long reportId = 999L;
      Long studyMemberId = 1L;
      String reportReason = "부적절한 행동";
      StudyMember studyMember = applied(studyMemberId, STUDY_ID, MEMBER_ID, "참여 메시지");
      RepostStudyMemberRequest request = new RepostStudyMemberRequest(reportReason);

      when(idGenerator.nextId()).thenReturn(reportId);
      when(studyMemberRepository.getByMemberIdAndStudyId(MEMBER_ID, STUDY_ID))
          .thenReturn(studyMember);

      // when
      reportStudyMemberService.reportStudyMember(STUDY_ID, MEMBER_ID, request);

      // then
      verify(studyMemberReportRepository).save(reportCaptor.capture());

      StudyMemberReport savedReport = reportCaptor.getValue();
      assertThat(savedReport.getId()).isEqualTo(reportId);
      assertThat(savedReport.getStudyMemberId()).isEqualTo(studyMemberId);
      assertThat(savedReport.getReportReason()).isEqualTo(reportReason);
    }

    @Test
    @DisplayName("신고 사유가 비어있어도 신고가 저장된다")
    void should_save_report_even_when_reason_is_empty() {
      // given
      Long reportId = 999L;
      Long studyMemberId = 1L;
      String reportReason = "";
      StudyMember studyMember = applied(studyMemberId, STUDY_ID, MEMBER_ID, "참여 메시지");
      RepostStudyMemberRequest request = new RepostStudyMemberRequest(reportReason);

      when(idGenerator.nextId()).thenReturn(reportId);
      when(studyMemberRepository.getByMemberIdAndStudyId(MEMBER_ID, STUDY_ID))
          .thenReturn(studyMember);

      // when
      reportStudyMemberService.reportStudyMember(STUDY_ID, MEMBER_ID, request);

      // then
      verify(studyMemberReportRepository).save(any(StudyMemberReport.class));
    }
  }
}
