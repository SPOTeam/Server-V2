package kr.spot.schedule.infrastructure.jpa;

import java.time.LocalDateTime;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.schedule.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

  boolean existsByStudyIdAndStartAtLessThanAndEndAtGreaterThan(
      Long studyId, LocalDateTime endAt, LocalDateTime startAt);

  default Schedule getById(long scheduleId) {
    return findById(scheduleId).orElseThrow(
        () -> new GeneralException(ErrorStatus._SCHEDULE_NOT_FOUND));
  }
}
