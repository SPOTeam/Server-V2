package kr.spot.application.query;

import java.util.List;
import kr.spot.domain.Member;
import kr.spot.domain.association.PreferredCategory;
import kr.spot.domain.association.PreferredRegion;
import kr.spot.infrastructure.jpa.MemberRepository;
import kr.spot.infrastructure.jpa.PreferredCategoryRepository;
import kr.spot.infrastructure.jpa.PreferredRegionRepository;
import kr.spot.ports.StudyParticipationInfoPort;
import kr.spot.ports.dto.StudyParticipationInfo;
import kr.spot.presentation.query.dto.response.GetMemberInfoResponse;
import kr.spot.presentation.query.dto.response.GetMemberInfoResponse.StudyParticipationInfoResponse;
import kr.spot.presentation.query.dto.response.GetMemberNameResponse;
import kr.spot.presentation.query.dto.response.GetMemberPreferCategoryResponse;
import kr.spot.presentation.query.dto.response.GetMemberPreferRegionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMemberInfoService {

  private final StudyParticipationInfoPort studyParticipationInfoPort;
  private final MemberRepository memberRepository;
  private final PreferredCategoryRepository preferredCategoryRepository;
  private final PreferredRegionRepository preferredRegionRepository;

  public GetMemberNameResponse getMemberName(Long memberId) {
    Member member = memberRepository.getMemberById(memberId);
    return GetMemberNameResponse.from(member.getName());
  }

  public GetMemberInfoResponse getMemberInfo(Long memberId) {
    Member member = memberRepository.getMemberById(memberId);

    StudyParticipationInfo studyParticipationInfo = studyParticipationInfoPort.getStudyParticipationInfo(
        memberId);
    
    return GetMemberInfoResponse.from(
        member.getId(),
        member.getName(),
        member.getProfileImageUrl(),
        member.getLoginType(),
        member.getEmail().getValue(),
        StudyParticipationInfoResponse.from(
            studyParticipationInfo.participatingStudyCount(),
            studyParticipationInfo.recruitingStudyCount(),
            studyParticipationInfo.appliedStudyCount()
        )
    );
  }

  public GetMemberPreferCategoryResponse getMemberPreferCategories(Long memberId) {
    List<PreferredCategory> preferredCategories = preferredCategoryRepository.findAllByMemberId(
        memberId);

    List<String> categoryCodes = preferredCategories.stream()
        .map(PreferredCategory::getCategory)
        .toList();

    return GetMemberPreferCategoryResponse.from(categoryCodes, categoryCodes.size());
  }

  public GetMemberPreferRegionResponse getMemberPreferRegions(Long memberId) {
    List<PreferredRegion> preferredRegions = preferredRegionRepository.findAllByMemberId(
        memberId);

    List<String> regionCodes = preferredRegions.stream()
        .map(PreferredRegion::getRegionCode)
        .toList();

    return GetMemberPreferRegionResponse.from(regionCodes, regionCodes.size());
  }
}
