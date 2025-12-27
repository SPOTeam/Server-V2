package kr.spot.study.application.command;

import java.util.List;
import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.Decision;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.study.presentation.command.dto.request.ApplyStudyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ApplyStudyService {

  private static final List<StudyMemberStatus> ACTIVE_APPLICATION_STATUSES = List.of(
      StudyMemberStatus.APPLIED,
      StudyMemberStatus.AWAITING_SELF_APPROVAL,
      StudyMemberStatus.APPROVED
  );

  private final IdGenerator idGenerator;
  private final StudyRepository studyRepository;
  private final StudyMemberRepository studyMemberRepository;

  public void processStudyApplication(
      Long applicationId,
      Long requesterId,
      Decision decision
  ) {
    StudyMember application = studyMemberRepository.getStudyMemberById(applicationId);
    Study study = studyRepository.getStudyById(application.getStudyId());

    study.processApplication(application, requesterId, decision);
  }

  public void decideFinalParticipation(
      Long applicationId,
      Long requesterId,
      Decision decision
  ) {
    StudyMember application = studyMemberRepository.getStudyMemberById(applicationId);

    application.decideFinalByApplicant(requesterId, decision);
  }

  public void applyStudy(Long studyId, Long memberId, ApplyStudyRequest request) {
    Study study = studyRepository.getStudyById(studyId);
    validateIsAlreadyApplied(studyId, memberId);

    StudyMember application = study.receiveApplication(
        idGenerator.nextId(),
        memberId,
        request.message()
    );
    studyMemberRepository.save(application);
  }

  private void validateIsAlreadyApplied(Long studyId, Long memberId) {
    if (isAlreadyAppliedThisStudy(studyId, memberId)) {
      throw new GeneralException(ErrorStatus._STUDY_ALREADY_APPLIED);
    }
  }

  private boolean isAlreadyAppliedThisStudy(Long studyId, Long memberId) {
    return studyMemberRepository.existsByStudyIdAndMemberIdAndStudyMemberStatusIn(
        studyId, memberId, ACTIVE_APPLICATION_STATUSES
    );
  }
}
