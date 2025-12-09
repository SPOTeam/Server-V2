package kr.spot.application.query;

import java.util.List;
import java.util.Map;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.associations.StudyMember;
import kr.spot.domain.enums.StudyMemberStatus;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.StudyMemberRepositoryCustom;
import kr.spot.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.infrastructure.jpa.querydsl.dto.StudyApplicationInfo;
import kr.spot.ports.GetMemberInfoPort;
import kr.spot.ports.dto.MemberInfoResponse;
import kr.spot.presentation.query.dto.response.GetAppliesResponse;
import kr.spot.presentation.query.dto.response.GetAppliesResponse.Apply;
import kr.spot.presentation.query.dto.response.GetMyAppliedStudyResponse;
import kr.spot.presentation.query.dto.response.GetMyAppliedStudyResponse.MyAppliedStudy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetStudyApplicationService {

  private final GetMemberInfoPort getMemberInfoPort;
  private final StudyMemberRepository studyMemberRepository;
  private final StudyMemberRepositoryCustom studyMemberRepositoryCustom;

  public GetMyAppliedStudyResponse getMyAppliedStudy(Long memberId) {
    List<StudyApplicationInfo> myAppliedStudiesInfo = getMyAppliedStudiesWithStudyInfo(memberId);
    return getGetMyAppliedStudyResponse(myAppliedStudiesInfo);
  }

  public GetAppliesResponse getStudyApplications(Long studyId, Long memberId) {
    validateIsStudyLeader(studyId, memberId);
    List<StudyMember> applications = getApplications(studyId);
    List<Long> memberIds = getApplicantsIds(applications);
    Map<Long, MemberInfoResponse> memberInfos = getMemberInfoPort.getMemberInfo(memberIds);
    return GetAppliesResponse.of(getApplies(applications, memberInfos));
  }

  private GetMyAppliedStudyResponse getGetMyAppliedStudyResponse(
      List<StudyApplicationInfo> myAppliedStudiesInfo) {
    return GetMyAppliedStudyResponse.of(myAppliedStudiesInfo.stream()
        .map(info -> new MyAppliedStudy(
                info.getStudyMemberId(),
                info.getStudyId(),
                info.getStudyName(),
                info.getStudyProfileImageUrl()
            )
        ).toList());
  }

  private List<Long> getApplicantsIds(List<StudyMember> applications) {
    return applications.stream()
        .map(StudyMember::getMemberId)
        .toList();
  }

  private List<StudyMember> getApplications(Long studyId) {
    return studyMemberRepositoryCustom
        .findApplicationsByStudyIdAndStatus(studyId, StudyMemberStatus.AWAITING_SELF_APPROVAL);
  }

  private List<StudyApplicationInfo> getMyAppliedStudiesWithStudyInfo(Long memberId) {
    return studyMemberRepositoryCustom
        .findMyAppliedStudiesWithStudyInfo(memberId, StudyMemberStatus.AWAITING_SELF_APPROVAL);
  }

  private void validateIsStudyLeader(Long studyId, Long memberId) {
    if (!studyMemberRepository.existsByStudyIdAndMemberIdAndStudyMemberStatus(
        studyId, memberId, kr.spot.domain.enums.StudyMemberStatus.OWNER)) {
      throw new GeneralException(ErrorStatus._ONLY_LEADER_CAN_ACCESS);
    }
  }

  private List<Apply> getApplies(List<StudyMember> applications,
      Map<Long, MemberInfoResponse> memberInfos) {
    return applications.stream()
        .map(application -> {
          MemberInfoResponse memberInfo = memberInfos.get(application.getMemberId());
          return Apply.of(
              application.getId(),
              application.getMemberId(),
              memberInfo.name(),
              application.getMessage(),
              memberInfo.profileImageUrl()
          );
        }).toList();
  }
}
