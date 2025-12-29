package kr.spot.study.application.validator;

import java.util.List;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyAccessValidator {

  private final StudyRepository studyRepository;
  private final StudyMemberRepository studyMemberRepository;

  public void validateStudyLeader(Long studyId, Long memberId) {
    Study study = studyRepository.getStudyById(studyId);
    study.validateIsStudyOwner(memberId);
  }

  public void validateStudyMember(Long studyId, Long memberId) {
    boolean isMember = studyMemberRepository.existsByStudyIdAndMemberIdAndStudyMemberStatusIn(
        studyId,
        memberId,
        List.of(StudyMemberStatus.OWNER, StudyMemberStatus.APPROVED)
    );

    if (!isMember) {
      throw new GeneralException(ErrorStatus._STUDY_ACCESS_DENIED);
    }
  }

  public boolean isStudyMember(Long studyId, Long memberId) {
    return studyMemberRepository.existsByStudyIdAndMemberIdAndStudyMemberStatusIn(
        studyId,
        memberId,
        List.of(StudyMemberStatus.OWNER, StudyMemberStatus.APPROVED)
    );
  }
}
