package kr.spot.presentation.query;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.application.query.GetStudyInfoTestService;
import kr.spot.presentation.query.dto.request.StudySearchRequestForEqual;
import kr.spot.presentation.query.dto.request.StudySearchRequestForRange;
import kr.spot.presentation.query.dto.response.GetStudyOverviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "스터디 조회 테스트")
@RestController
@RequestMapping("/api/test/studies")
@RequiredArgsConstructor
public class StudyQueryTestController {

  private final GetStudyInfoTestService service;

  @GetMapping("/range")
  public ResponseEntity<GetStudyOverviewResponse> getStudyInfoByRange(
      StudySearchRequestForRange request
  ) {
    return ResponseEntity.ok(
        service.getStudyInfoByRange(request.hasFee(), request.fee(), request.recruitingStatus(),
            request.categories(), request.cursor(), request.limit())
    );
  }

  @GetMapping("/category")
  public ResponseEntity<GetStudyOverviewResponse> getStudyInfoByCategory(
      StudySearchRequestForEqual request
  ) {
    log.info("feeCategory: {}", request.feeCategory());
    return ResponseEntity.ok(
        service.getStudyInfoByCategory(request.feeCategory(), request.recruitingStatus(),
            request.categories(), request.cursor(), request.limit())
    );
  }
}
