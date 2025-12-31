package kr.spot.study.application.query;

import java.util.List;
import java.util.Map;
import kr.spot.ports.GetMemberInfoPort;
import kr.spot.ports.dto.MemberInfoResponse;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.associations.StudyCategory;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.associations.StudyStats;
import kr.spot.study.domain.enums.Category;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyCategoryRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyStatsRepository;
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
  private final StudyStatsRepository studyStatsRepository;
  private final StudyMemberRepository studyMemberRepository;
  private final StudyViewCountService studyViewCountService;
  private final GetMemberInfoPort getMemberInfoPort;

  public GetStudyInfoResponse getStudyInfo(long studyId, long viewerId) {
    Study study = findStudy(studyId);
    StudyStats stats = findStudyStats(studyId);
    List<Category> categories = findCategories(studyId);
    Statistics statistics = buildStatistics(study, stats, studyId, viewerId);

    return toStudyInfoResponse(study, categories, statistics);
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

  private StudyStats findStudyStats(long studyId) {
    return studyStatsRepository.getByStudyId(studyId);
  }

  private List<Category> findCategories(long studyId) {
    return studyCategoryRepository.findAllByStudyId(studyId).stream()
        .map(StudyCategory::getCategory)
        .toList();
  }

  private Statistics buildStatistics(Study study, StudyStats stats, long studyId, long viewerId) {
    long displayViewCount = studyViewCountService.calculateDisplayViewCount(stats, studyId,
        viewerId);
    return Statistics.of(
        study.getMaxMembers(),
        study.getCurrentMembers(),
        stats.getLikeCount(),
        displayViewCount
    );
  }

  private GetStudyInfoResponse toStudyInfoResponse(Study study, List<Category> categories,
      Statistics statistics) {
    return GetStudyInfoResponse.of(
        study.getName(),
        study.getDescription(),
        study.getImageUrl(),
        categories,
        statistics
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
