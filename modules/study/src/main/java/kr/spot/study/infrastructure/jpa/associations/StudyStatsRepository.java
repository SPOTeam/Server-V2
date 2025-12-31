package kr.spot.study.infrastructure.jpa.associations;

import java.util.Optional;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.study.domain.associations.StudyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyStatsRepository extends JpaRepository<StudyStats, Long> {

  Optional<StudyStats> findByStudyId(long studyId);

  default StudyStats getByStudyId(long studyId) {
    return findByStudyId(studyId)
        .orElseThrow(() -> new GeneralException(ErrorStatus._STUDY_NOT_FOUND));
  }

  @Modifying
  @Query("""
      update StudyStats s
         set s.viewCount = s.viewCount + :delta,
             s.updatedAt = CURRENT_TIMESTAMP
       where s.studyId = :studyId and s.status = 'ACTIVE'
      """)
  int increaseViewBy(@Param("studyId") long studyId, @Param("delta") long delta);
}
