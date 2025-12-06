package kr.spot.application.query;

import java.util.List;
import kr.spot.application.mapper.StudyDTOMapper;
import kr.spot.domain.Study;
import kr.spot.domain.enums.Category;
import kr.spot.domain.enums.FeeCategory;
import kr.spot.domain.enums.RecruitingStatus;
import kr.spot.infrastructure.jpa.querydsl.StudyQueryTestRepository;
import kr.spot.presentation.query.dto.response.GetStudyOverviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetStudyInfoTestService {

  private final StudyQueryTestRepository repository;

  public GetStudyOverviewResponse getStudyInfoByRange(
      boolean hasFee,
      int fee,
      RecruitingStatus recruitingStatus,
      List<Category> categories,
      Long cursor,
      int limit
  ) {

    List<Study> studies = repository.findStudyWithRange(
        hasFee,
        fee,
        recruitingStatus,
        categories,
        cursor,
        limit + 1
    );

    return getGetStudyOverviewResponse(limit, studies);
  }

  public GetStudyOverviewResponse getStudyInfoByCategory(FeeCategory feeCategory,
      RecruitingStatus recruitingStatus,
      List<Category> categories,
      Long cursor,
      int limit) {

    List<Study> studies = repository.findStudyWithCategory(
        feeCategory,
        recruitingStatus,
        categories,
        cursor,
        limit + 1
    );

    return getGetStudyOverviewResponse(limit, studies);


  }

  private GetStudyOverviewResponse getGetStudyOverviewResponse(int limit,
      List<Study> studies) {
    boolean hasNext = studies.size() > limit;
    List<Study> pageContent = hasNext ? studies.subList(0, limit) : studies;
    Long nextCursor = hasNext ? pageContent.getLast().getId() : null;

    return StudyDTOMapper.toDTO(pageContent, hasNext, nextCursor, (long) pageContent.size());
  }
}
