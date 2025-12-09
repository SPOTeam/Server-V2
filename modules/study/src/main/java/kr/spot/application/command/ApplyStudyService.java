package kr.spot.application.command;

import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.associations.StudyMember;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.StudyRepository;
import kr.spot.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.presentation.command.dto.request.ApplyStudyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ApplyStudyService {

  private final IdGenerator idGenerator;
  private final StudyRepository studyRepository;
  private final StudyMemberRepository studyMemberRepository;

  public void applyStudy(Long studyId, Long memberId, ApplyStudyRequest request) {
    validateIsExistMember(studyId);
    studyMemberRepository.save(
        StudyMember.apply(
            idGenerator.nextId(),
            studyId,
            memberId,
            request.message()
        ));
  }

  private void validateIsExistMember(Long studyId) {
    if (!studyRepository.existsById(studyId)) {
      throw new GeneralException(ErrorStatus._STUDY_NOT_FOUND);
    }
  }
}