package kr.spot.schedule.infrastructure.jpa.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import kr.spot.schedule.domain.QSchedule;
import kr.spot.schedule.domain.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScheduleQueryRepository {

  private final JPAQueryFactory query;

  public List<Schedule> findMonthlySchedules(Long studyId, LocalDate date) {
    QSchedule schedule = QSchedule.schedule;

    YearMonth yearMonth = YearMonth.from(date);
    LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
    LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

    return query
        .selectFrom(schedule)
        .where(
            schedule.studyId.eq(studyId),
            schedule.startAt.between(startOfMonth, endOfMonth)
                .or(schedule.endAt.between(startOfMonth, endOfMonth))
        )
        .orderBy(schedule.startAt.asc())
        .fetch();
  }

  public List<Schedule> findWeeklySchedules(Long studyId, LocalDate date) {
    QSchedule schedule = QSchedule.schedule;

    LocalDate monday = date.with(DayOfWeek.MONDAY);
    LocalDate sunday = date.with(DayOfWeek.SUNDAY);
    LocalDateTime startOfWeek = monday.atStartOfDay();
    LocalDateTime endOfWeek = sunday.atTime(LocalTime.MAX);

    return query
        .selectFrom(schedule)
        .where(
            schedule.studyId.eq(studyId),
            schedule.startAt.between(startOfWeek, endOfWeek)
                .or(schedule.endAt.between(startOfWeek, endOfWeek))
        )
        .orderBy(schedule.startAt.asc())
        .fetch();
  }

  public List<Schedule> findUpcomingSchedules(Long studyId, int limit) {
    QSchedule schedule = QSchedule.schedule;

    LocalDateTime now = LocalDateTime.now();

    return query
        .selectFrom(schedule)
        .where(
            schedule.studyId.eq(studyId),
            schedule.startAt.goe(now)
        )
        .orderBy(schedule.startAt.asc())
        .limit(limit)
        .fetch();
  }
}
