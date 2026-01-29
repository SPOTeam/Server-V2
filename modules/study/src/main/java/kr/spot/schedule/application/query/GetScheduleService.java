package kr.spot.schedule.application.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import kr.spot.schedule.domain.Schedule;
import kr.spot.schedule.infrastructure.jpa.querydsl.ScheduleQueryRepository;
import kr.spot.schedule.presentation.query.dto.GetScheduleListResponse;
import kr.spot.schedule.presentation.query.dto.GetScheduleListResponse.ScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetScheduleService {

  private static final int UPCOMING_LIMIT = 2;

  private final ScheduleQueryRepository scheduleQueryRepository;

  public GetScheduleListResponse getMonthlySchedules(long studyId, int year, int month, long memberId) {
    LocalDate date = LocalDate.of(year, month, 1);
    List<Schedule> schedules = scheduleQueryRepository.findMonthlySchedules(studyId, date);
    return toResponse(schedules, memberId);
  }

  public GetScheduleListResponse getWeeklySchedules(long studyId, LocalDate date, long memberId) {
    List<Schedule> schedules = scheduleQueryRepository.findWeeklySchedules(studyId, date);
    return toResponse(schedules, memberId);
  }

  public GetScheduleListResponse getUpcomingSchedules(long studyId, long memberId) {
    List<Schedule> schedules = scheduleQueryRepository.findUpcomingSchedules(studyId, UPCOMING_LIMIT);
    return toResponse(schedules, memberId);
  }

  private GetScheduleListResponse toResponse(List<Schedule> schedules, long memberId) {
    LocalDateTime now = LocalDateTime.now();

    List<ScheduleResponse> responses = schedules.stream()
        .map(schedule -> ScheduleResponse.from(
            schedule.getId(),
            schedule.getTitle(),
            schedule.getStartAt(),
            schedule.getEndAt(),
            schedule.isOngoing(now),
            schedule.getCreatorId() != null && schedule.getCreatorId() == memberId
        ))
        .toList();

    return GetScheduleListResponse.from(responses);
  }
}
