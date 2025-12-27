package kr.spot.study.application.query;

import java.util.List;
import java.util.Map;
import kr.spot.ports.GetMemberInfoPort;
import kr.spot.ports.dto.MemberInfoResponse;
import kr.spot.study.application.validator.StudyAccessValidator;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.infrastructure.jpa.StudyMemberRepositoryCustom;
import kr.spot.study.infrastructure.jpa.querydsl.dto.StudyApplicationInfo;
import kr.spot.study.presentation.query.dto.response.GetAppliesResponse;
import kr.spot.study.presentation.query.dto.response.GetAppliesResponse.Apply;
import kr.spot.study.presentation.query.dto.response.GetMyAppliedStudyResponse;
import kr.spot.study.presentation.query.dto.response.GetMyAppliedStudyResponse.MyAppliedStudy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetStudyApplicationService {


  private final GetMemberInfoPort getMemberInfoPort;
  private final StudyMemberRepositoryCustom studyMemberRepositoryCustom;
  private final StudyAccessValidator studyAccessValidator;

  public GetMyAppliedStudyResponse getMyAppliedStudy(Long memberId) {
    List<StudyApplicationInfo> applications =
        studyMemberRepositoryCustom.findMyAppliedStudiesWithStudyInfo(
            memberId,
            StudyMemberStatus.AWAITING_SELF_APPROVAL
        );

    return GetMyAppliedStudyResponse.of(
        applications.stream()
            .map(info -> new MyAppliedStudy(
                info.getStudyMemberId(),
                info.getStudyId(),
                info.getStudyName(),
                info.getStudyProfileImageUrl()
            ))
            .toList()
    );
  }

  public GetAppliesResponse getStudyApplications(Long studyId, Long requesterId) {

    studyAccessValidator.validateStudyLeader(studyId, requesterId);

    List<StudyMember> applications =
        studyMemberRepositoryCustom.findApplicationsByStudyIdAndStatus(
            studyId,
            StudyMemberStatus.APPLIED
        );

    if (applications.isEmpty()) {
      return GetAppliesResponse.of(List.of());
    }

    List<Long> applicantIds = applications.stream()
        .map(StudyMember::getMemberId)
        .distinct()
        .toList();

    Map<Long, MemberInfoResponse> memberInfoMap =
        getMemberInfoPort.getMemberInfo(applicantIds);

    List<Apply> applies = applications.stream()
        .map(application -> {
          MemberInfoResponse memberInfo = memberInfoMap.get(application.getMemberId());
          return Apply.of(
              application.getId(),
              application.getMemberId(),
              memberInfo.name(),
              application.getMessage(),
              memberInfo.profileImageUrl()
          );
        })
        .toList();

    return GetAppliesResponse.of(applies);
  }
}
