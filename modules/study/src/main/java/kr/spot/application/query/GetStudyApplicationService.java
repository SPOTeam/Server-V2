package kr.spot.application.query;

import java.util.List;
import kr.spot.domain.enums.StudyMemberStatus;
import kr.spot.infrastructure.jpa.StudyMemberRepositoryCustom;
import kr.spot.infrastructure.jpa.querydsl.dto.StudyApplicationInfo;
import kr.spot.presentation.query.dto.response.GetMyAppliedStudyResponse;
import kr.spot.presentation.query.dto.response.GetMyAppliedStudyResponse.MyAppliedStudy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetStudyApplicationService {

  private final StudyMemberRepositoryCustom studyMemberRepository;

  public GetMyAppliedStudyResponse getMyAppliedStudy(Long memberId) {
    List<StudyApplicationInfo> myAppliedStudiesInfo = getMyAppliedStudiesWithStudyInfo(memberId);

    return GetMyAppliedStudyResponse.of(myAppliedStudiesInfo.stream()
        .map(info -> new MyAppliedStudy(
                info.getStudyMemberId(),
                info.getStudyId(),
                info.getStudyName(),
                info.getStudyProfileImageUrl()
            )
        ).toList());
  }

  private List<StudyApplicationInfo> getMyAppliedStudiesWithStudyInfo(Long memberId) {
    return studyMemberRepository
        .findMyAppliedStudiesWithStudyInfo(memberId, StudyMemberStatus.AWAITING_SELF_APPROVAL);
  }

}
