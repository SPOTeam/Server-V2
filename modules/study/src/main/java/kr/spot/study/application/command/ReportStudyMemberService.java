package kr.spot.study.application.command;

import jakarta.transaction.Transactional;
import kr.spot.IdGenerator;
import kr.spot.study.domain.associations.StudyMemberReport;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberReportRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.study.presentation.command.dto.request.RepostStudyMemberRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportStudyMemberService {

  private final IdGenerator idGenerator;
  private final StudyMemberRepository studyMemberRepository;
  private final StudyMemberReportRepository studyMemberReportRepository;

  public void reportStudyMember(Long studyId, Long memberId, RepostStudyMemberRequest request) {
    var studyMember = studyMemberRepository.getByMemberIdAndStudyId(memberId, studyId);

    StudyMemberReport studyMemberReport = StudyMemberReport.create(idGenerator.nextId(),
        studyMember.getId(), request.reason());

    studyMemberReportRepository.save(studyMemberReport);
  }
}
