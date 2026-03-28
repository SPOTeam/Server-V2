package kr.spot.study.infrastructure.jpa;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.enums.RecruitingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRepository extends JpaRepository<Study, Long> {

  default Study getStudyById(Long studyId) {
    return findById(studyId)
        .orElseThrow(() -> new GeneralException(ErrorStatus._STUDY_NOT_FOUND));
  }

  long countByLeaderIdAndRecruitingStatus(long leaderId, RecruitingStatus recruitingStatus);

  boolean existsByLeaderId(long leaderId);

  @Modifying
  @Query("""
      update Study s
         set s.viewCount = s.viewCount + :delta,
             s.updatedAt = CURRENT_TIMESTAMP
       where s.id = :studyId
      """)
  int increaseViewBy(@Param("studyId") long studyId, @Param("delta") long delta);

  @Modifying
  @Query("update Study s set s.likeCount = s.likeCount + 1 where s.id = :studyId")
  int increaseLike(@Param("studyId") long studyId);

  @Modifying
  @Query("update Study s set s.likeCount = case when s.likeCount > 0 then s.likeCount - 1 else 0 end where s.id = :studyId")
  int decreaseLike(@Param("studyId") long studyId);
}
