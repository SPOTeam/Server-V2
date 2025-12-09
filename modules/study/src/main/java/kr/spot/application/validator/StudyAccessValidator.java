package kr.spot.application.validator;

import java.util.List;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.Study;
import kr.spot.domain.enums.StudyMemberStatus;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.StudyRepository;
import kr.spot.infrastructure.jpa.associations.StudyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyAccessValidator {

  private final StudyRepository studyRepository;
  private final StudyMemberRepository studyMemberRepository;

  /**
   * 스터디장 권한 검증
   */
  public void validateStudyLeader(Long studyId, Long memberId) {
    Study study = studyRepository.getStudyById(studyId);
    study.validateIsStudyOwner(memberId);
  }

  /**
   * 스터디 멤버 권한 검증 (스터디장 또는 멤버)
   */
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
}