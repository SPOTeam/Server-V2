package kr.spot.infrastructure.jpa.associations;

import java.util.List;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.associations.StudyMember;
import kr.spot.domain.enums.StudyMemberStatus;
import kr.spot.exception.GeneralException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

  default StudyMember getStudyMemberById(Long studyMemberId) {
    return findById(studyMemberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus._STUDY_MEMBER_NOT_FOUND));
  }

  boolean existsByStudyIdAndMemberIdAndStudyMemberStatusIn(Long studyId, Long memberId,
      List<StudyMemberStatus> studyMemberStatuses);

}
