package kr.spot.schedule.infrastructure.jpa;

import java.util.List;
import kr.spot.schedule.domain.ScheduleExclusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleExclusionRepository extends JpaRepository<ScheduleExclusion, Long> {

  boolean existsByScheduleIdAndMemberId(long scheduleId, long memberId);

  @Query("SELECT se.scheduleId FROM ScheduleExclusion se "
      + "WHERE se.memberId = :memberId AND se.scheduleId IN :scheduleIds")
  List<Long> findExcludedScheduleIds(
      @Param("memberId") long memberId,
      @Param("scheduleIds") List<Long> scheduleIds);

  @Query("SELECT se.memberId FROM ScheduleExclusion se "
      + "WHERE se.scheduleId = :scheduleId")
  List<Long> findExcludedMemberIds(@Param("scheduleId") long scheduleId);
}
