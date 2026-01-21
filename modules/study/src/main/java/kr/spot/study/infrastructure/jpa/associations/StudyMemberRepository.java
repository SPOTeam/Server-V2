package kr.spot.study.infrastructure.jpa.associations;

import java.util.List;
import java.util.Set;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.StudyMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

  default StudyMember getStudyMemberById(Long studyMemberId) {
    return findById(studyMemberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus._STUDY_MEMBER_NOT_FOUND));
  }

  boolean existsByStudyIdAndMemberIdAndStudyMemberStatusIn(Long studyId, Long memberId,
      List<StudyMemberStatus> studyMemberStatuses);

  List<StudyMember> findAllByStudyIdAndStudyMemberStatusIn(Long studyId,
      List<StudyMemberStatus> studyMemberStatuses);

  long countByMemberIdAndStudyMemberStatusIn(long memberId,
      List<StudyMemberStatus> studyMemberStatuses);

  long countByMemberIdAndStudyMemberStatus(long memberId, StudyMemberStatus studyMemberStatus);

  void deleteByMemberId(long memberId);

  Set<Long> findStudyIdsByMemberIdAndStudyMemberStatus(long memberId,
      StudyMemberStatus studyMemberStatus);
}
