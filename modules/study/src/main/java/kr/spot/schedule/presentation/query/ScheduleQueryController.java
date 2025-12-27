package kr.spot.schedule.presentation.query;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 일정")
@RestController
@RequestMapping("/api/studies/{studyId}/schedules")
@RequiredArgsConstructor
public class ScheduleQueryController {

  // TODO: 월 단위 조회, 주 단위 조회 필요
}
