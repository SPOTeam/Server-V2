package kr.spot.study.infrastructure.jpa.associations;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.StudyMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

  default StudyMember getStudyMemberById(Long studyMemberId) {
    return findById(studyMemberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus._STUDY_MEMBER_NOT_FOUND));
  }

  default StudyMember getByMemberIdAndStudyId(Long memberId, Long studyId) {
    return findByMemberIdAndStudyId(memberId, studyId)
        .orElseThrow(() -> new GeneralException(ErrorStatus._STUDY_MEMBER_NOT_FOUND));
  }

  Optional<StudyMember> findByMemberIdAndStudyId(Long memberId, Long studyId);

  boolean existsByStudyIdAndMemberIdAndStudyMemberStatusIn(Long studyId, Long memberId,
      List<StudyMemberStatus> studyMemberStatuses);

  List<StudyMember> findAllByStudyIdAndStudyMemberStatusIn(Long studyId,
      List<StudyMemberStatus> studyMemberStatuses);

  long countByMemberIdAndStudyMemberStatusIn(long memberId,
      List<StudyMemberStatus> studyMemberStatuses);

  long countByMemberIdAndStudyMemberStatus(long memberId, StudyMemberStatus studyMemberStatus);

  void deleteByMemberId(long memberId);

  @Query("SELECT sm.studyId FROM StudyMember sm "
      + "WHERE sm.memberId = :memberId "
      + "AND sm.studyMemberStatus = :studyMemberStatus")
  Set<Long> findStudyIdsByMemberIdAndStudyMemberStatus(
      @Param("memberId") long memberId,
      @Param("studyMemberStatus") StudyMemberStatus studyMemberStatus);

  @Query("SELECT sm.studyId FROM StudyMember sm "
      + "JOIN Study s ON sm.studyId = s.id "
      + "WHERE sm.memberId = :memberId "
      + "AND sm.studyMemberStatus = 'OWNER' "
      + "AND s.currentMembers = 1")
  Set<Long> findAloneOwnerStudyIds(@Param("memberId") long memberId);
}
