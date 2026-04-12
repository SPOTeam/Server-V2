package kr.spot.study.application.query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import kr.spot.ports.GetMemberInfoPort;
import kr.spot.ports.dto.MemberInfoResponse;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.associations.StudyCategory;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.associations.StudyRegion;
import kr.spot.study.domain.associations.StudyStyle;
import kr.spot.study.domain.enums.Category;
import kr.spot.study.domain.enums.Style;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.domain.enums.ViewerStatus;
import kr.spot.study.domain.vo.Fee;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyCategoryRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyRegionRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyLikeRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyStyleRepository;
import kr.spot.study.presentation.query.dto.response.GetStudyInfoResponse;
import kr.spot.study.presentation.query.dto.response.GetStudyInfoResponse.Statistics;
import kr.spot.study.presentation.query.dto.response.GetStudyMembersResponse;
import kr.spot.study.presentation.query.dto.response.GetStudyMembersResponse.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetStudyDetailService {

  private static final List<StudyMemberStatus> ACTIVE_MEMBER_STATUSES =
      List.of(StudyMemberStatus.OWNER, StudyMemberStatus.APPROVED);

  private final StudyRepository studyRepository;
  private final StudyCategoryRepository studyCategoryRepository;
  private final StudyMemberRepository studyMemberRepository;
  private final StudyStyleRepository studyStyleRepository;
  private final StudyRegionRepository studyRegionRepository;
  private final StudyLikeRepository studyLikeRepository;
  private final StudyViewCountService studyViewCountService;
  private final GetMemberInfoPort getMemberInfoPort;

  public GetStudyInfoResponse getStudyInfo(long studyId, long viewerId) {
    Study study = findStudy(studyId);
    List<Category> categories = findCategories(studyId);
    List<Style> styles = findStyles(studyId);
    List<String> regionCodes = findRegionCodes(studyId);
    Statistics statistics = buildStatistics(study, studyId, viewerId);
    ViewerStatus viewerStatus = resolveViewerStatus(studyId, viewerId);
    boolean isLiked = studyLikeRepository.existsByStudyIdAndMemberId(studyId, viewerId);

    return toStudyInfoResponse(study, categories, styles, regionCodes, statistics, viewerStatus,
        isLiked);
  }

  public GetStudyMembersResponse getStudyMembers(long studyId) {
    List<StudyMember> studyMembers = findActiveStudyMembers(studyId);
    Map<Long, MemberInfoResponse> memberInfoMap = fetchMemberInfos(studyMembers);
    List<MemberResponse> members = toMemberResponses(studyMembers, memberInfoMap);

    return GetStudyMembersResponse.of(members, members.size());
  }

  private Study findStudy(long studyId) {
    return studyRepository.getStudyById(studyId);
  }

  private List<Category> findCategories(long studyId) {
    return studyCategoryRepository.findAllByStudyId(studyId).stream()
        .map(StudyCategory::getCategory)
        .toList();
  }

  private List<Style> findStyles(long studyId) {
    return studyStyleRepository.findAllByStudyId(studyId).stream()
        .map(StudyStyle::getStyle)
        .toList();
  }

  private List<String> findRegionCodes(long studyId) {
    return studyRegionRepository.findAllByStudyId(studyId).stream()
        .map(StudyRegion::getRegionCode)
        .toList();
  }

  private Statistics buildStatistics(Study study, long studyId, long viewerId) {
    long displayViewCount = studyViewCountService.calculateDisplayViewCount(study, studyId,
        viewerId);
    return Statistics.of(
        study.getMaxMembers(),
        study.getCurrentMembers(),
        study.getLikeCount(),
        displayViewCount
    );
  }

  private ViewerStatus resolveViewerStatus(long studyId, long viewerId) {
    Optional<StudyMember> studyMember = studyMemberRepository
        .findByMemberIdAndStudyId(viewerId, studyId);

    if (studyMember.isEmpty()) {
      return ViewerStatus.NOT_APPLIED;
    }

    return switch (studyMember.get().getStudyMemberStatus()) {
      case OWNER -> ViewerStatus.OWNER;
      case APPROVED -> ViewerStatus.APPROVED;
      case APPLIED, AWAITING_SELF_APPROVAL -> ViewerStatus.APPLIED;
      default -> ViewerStatus.NOT_APPLIED;
    };
  }

  private GetStudyInfoResponse toStudyInfoResponse(Study study, List<Category> categories,
      List<Style> styles, List<String> regionCodes, Statistics statistics,
      ViewerStatus viewerStatus, boolean isLiked) {
    Fee fee = study.getFee();
    boolean hasFee = fee != null && fee.isHasFee();
    Integer amount = fee != null ? fee.getAmount() : null;
    return GetStudyInfoResponse.of(
        study.getId(),
        study.getName(),
        study.getDescription(),
        study.getImageUrl(),
        study.getMaxMembers(),
        hasFee,
        amount,
        categories,
        styles,
        regionCodes,
        Boolean.TRUE.equals(study.getIsOnline()),
        isLiked,
        statistics,
        viewerStatus.name()
    );
  }

  private List<StudyMember> findActiveStudyMembers(long studyId) {
    return studyMemberRepository.findAllByStudyIdAndStudyMemberStatusIn(
        studyId, ACTIVE_MEMBER_STATUSES);
  }

  private Map<Long, MemberInfoResponse> fetchMemberInfos(List<StudyMember> studyMembers) {
    List<Long> memberIds = studyMembers.stream()
        .map(StudyMember::getMemberId)
        .toList();
    return getMemberInfoPort.getMemberInfo(memberIds);
  }

  private List<MemberResponse> toMemberResponses(List<StudyMember> studyMembers,
      Map<Long, MemberInfoResponse> memberInfoMap) {
    return studyMembers.stream()
        .map(studyMember -> toMemberResponse(studyMember, memberInfoMap))
        .toList();
  }

  private MemberResponse toMemberResponse(StudyMember studyMember,
      Map<Long, MemberInfoResponse> memberInfoMap) {
    MemberInfoResponse memberInfo = memberInfoMap.get(studyMember.getMemberId());
    boolean isOwner = studyMember.getStudyMemberStatus() == StudyMemberStatus.OWNER;

    return MemberResponse.of(
        studyMember.getMemberId(),
        memberInfo.name(),
        memberInfo.profileImageUrl(),
        isOwner
    );
  }
}
