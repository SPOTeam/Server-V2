package kr.spot.study.infrastructure.jpa.associations;

import kr.spot.study.domain.associations.StudyLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyLikeRepository extends JpaRepository<StudyLike, Long> {

  @Modifying
  @Query(
      value = """
          INSERT IGNORE INTO study_like(id, study_id, member_id, status, created_at, updated_at)
          VALUES (:id, :studyId, :memberId, 'ACTIVE', NOW(), NOW())
          """, nativeQuery = true)
  int saveStudyLike(@Param("id") long id,
      @Param("studyId") long studyId,
      @Param("memberId") long memberId);

  @Modifying
  @Query(value = """
      DELETE FROM study_like
       WHERE study_id = :studyId
         AND member_id = :memberId
      """, nativeQuery = true)
  int hardDelete(@Param("studyId") long studyId,
      @Param("memberId") long memberId);

  @Modifying
  @Query(value = "DELETE FROM study_like WHERE member_id = :memberId", nativeQuery = true)
  void deleteAllByMemberId(@Param("memberId") long memberId);
}
