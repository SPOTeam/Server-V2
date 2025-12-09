package kr.spot.infrastructure.jpa.associations;

import java.util.Optional;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.associations.StudyMember;
import kr.spot.domain.enums.StudyMemberStatus;
import kr.spot.exception.GeneralException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

  default StudyMember getById(Long id) {
    return findById(id)
        .orElseThrow(() -> new GeneralException(ErrorStatus._STUDY_MEMBER_NOT_FOUND));
  }

  boolean existsByStudyIdAndMemberIdAndStudyMemberStatus(Long studyId, Long memberId,
      StudyMemberStatus studyMemberStatus);

  Optional<StudyMember> findByStudyIdAndMemberId(Long studyId, Long memberId);
}
