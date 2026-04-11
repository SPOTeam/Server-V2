package kr.spot.study.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import kr.spot.ports.GetMemberInfoPort;
import kr.spot.ports.dto.MemberInfoResponse;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.associations.StudyCategory;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.Category;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.domain.vo.Fee;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyCategoryRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyRegionRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyStyleRepository;
import kr.spot.study.presentation.query.dto.response.GetStudyInfoResponse;
import kr.spot.study.presentation.query.dto.response.GetStudyMembersResponse;
import kr.spot.study.presentation.query.dto.response.GetStudyMembersResponse.MemberResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStudyDetailService 단위 테스트")
class GetStudyDetailServiceTest {

  @Mock
  StudyRepository studyRepository;

  @Mock
  StudyCategoryRepository studyCategoryRepository;

  @Mock
  StudyMemberRepository studyMemberRepository;

  @Mock
  StudyStyleRepository studyStyleRepository;

  @Mock
  StudyRegionRepository studyRegionRepository;

  @Mock
  StudyViewCountService studyViewCountService;

  @Mock
  GetMemberInfoPort getMemberInfoPort;

  @InjectMocks
  GetStudyDetailService sut;

  @Nested
  @DisplayName("getStudyInfo 메서드는")
  class GetStudyInfoTest {

    private final long studyId = 1L;
    private final long viewerId = 100L;

    @Test
    @DisplayName("스터디 상세 정보를 정상적으로 반환한다")
    void should_return_study_info_successfully() {
      // given
      Study study = createStudy(studyId, "알고리즘 스터디", 10, 5, 100L, 50L);
      List<StudyCategory> categories = List.of(
          StudyCategory.of(1L, studyId, Category.LANGUAGE),
          StudyCategory.of(2L, studyId, Category.CERTIFICATION)
      );
      long displayViewCount = 150L;

      given(studyRepository.getStudyById(studyId)).willReturn(study);
      given(studyCategoryRepository.findAllByStudyId(studyId)).willReturn(categories);
      given(studyStyleRepository.findAllByStudyId(studyId)).willReturn(List.of());
      given(studyRegionRepository.findAllByStudyId(studyId)).willReturn(List.of());
      given(studyViewCountService.calculateDisplayViewCount(study, studyId, viewerId))
          .willReturn(displayViewCount);
      given(studyMemberRepository.findByMemberIdAndStudyId(viewerId, studyId))
          .willReturn(Optional.empty());

      // when
      GetStudyInfoResponse result = sut.getStudyInfo(studyId, viewerId);

      // then
      assertThat(result.title()).isEqualTo("알고리즘 스터디");
      assertThat(result.maxMembers()).isEqualTo(10);
      assertThat(result.hasFee()).isFalse();
      assertThat(result.amount()).isEqualTo(0);
      assertThat(result.styles()).isEmpty();
      assertThat(result.regionCodes()).isEmpty();
      assertThat(result.isOnline()).isFalse();
      assertThat(result.description()).isEqualTo("스터디 설명");
      assertThat(result.categories()).containsExactly(Category.LANGUAGE, Category.CERTIFICATION);
      assertThat(result.statistics().totalMembers()).isEqualTo(10);
      assertThat(result.statistics().currentMembers()).isEqualTo(5);
      assertThat(result.statistics().likeCount()).isEqualTo(50L);
      assertThat(result.statistics().hitCount()).isEqualTo(displayViewCount);
      assertThat(result.viewerStatus()).isEqualTo("NOT_APPLIED");
    }

    @Test
    @DisplayName("카테고리가 없는 스터디도 정상적으로 반환한다")
    void should_return_study_info_with_empty_categories() {
      // given
      Study study = createStudy(studyId, "스터디", 5, 1, 10L, 5L);
      long displayViewCount = 10L;

      given(studyRepository.getStudyById(studyId)).willReturn(study);
      given(studyCategoryRepository.findAllByStudyId(studyId)).willReturn(List.of());
      given(studyStyleRepository.findAllByStudyId(studyId)).willReturn(List.of());
      given(studyRegionRepository.findAllByStudyId(studyId)).willReturn(List.of());
      given(studyViewCountService.calculateDisplayViewCount(study, studyId, viewerId))
          .willReturn(displayViewCount);
      given(studyMemberRepository.findByMemberIdAndStudyId(viewerId, studyId))
          .willReturn(Optional.empty());

      // when
      GetStudyInfoResponse result = sut.getStudyInfo(studyId, viewerId);

      // then
      assertThat(result.categories()).isEmpty();
      assertThat(result.statistics().hitCount()).isEqualTo(displayViewCount);
    }

    private Study createStudy(long id, String name, int maxMembers, int currentMembers,
        long viewCount, long likeCount) {
      Study study = Study.of(id, 1L, name, maxMembers, Fee.of(false, 0), "스터디 설명");
      ReflectionTestUtils.setField(study, "currentMembers", currentMembers);
      ReflectionTestUtils.setField(study, "viewCount", viewCount);
      ReflectionTestUtils.setField(study, "likeCount", likeCount);
      return study;
    }
  }

  @Nested
  @DisplayName("getStudyMembers 메서드는")
  class GetStudyMembersTest {

    private final long studyId = 1L;
    private final List<StudyMemberStatus> ACTIVE_STATUSES =
        List.of(StudyMemberStatus.OWNER, StudyMemberStatus.APPROVED);

    @Test
    @DisplayName("활성 멤버 목록을 정상적으로 반환한다")
    void should_return_active_members_successfully() {
      // given
      long ownerId = 10L;
      long memberId = 20L;
      StudyMember owner = StudyMember.create(1L, studyId, ownerId);
      StudyMember member = createApprovedMember(2L, studyId, memberId);

      MemberInfoResponse ownerInfo = MemberInfoResponse.of("스터디장", "https://image.url/owner.jpg");
      MemberInfoResponse memberInfo = MemberInfoResponse.of("멤버1", "https://image.url/member.jpg");

      given(studyMemberRepository.findAllByStudyIdAndStudyMemberStatusIn(studyId, ACTIVE_STATUSES))
          .willReturn(List.of(owner, member));
      given(getMemberInfoPort.getMemberInfo(List.of(ownerId, memberId)))
          .willReturn(Map.of(ownerId, ownerInfo, memberId, memberInfo));

      // when
      GetStudyMembersResponse result = sut.getStudyMembers(studyId);

      // then
      assertThat(result.totalMembers()).isEqualTo(2);
      assertThat(result.members()).hasSize(2);

      MemberResponse ownerResponse = result.members().stream()
          .filter(MemberResponse::isOwner)
          .findFirst()
          .orElseThrow();
      assertThat(ownerResponse.memberId()).isEqualTo(ownerId);
      assertThat(ownerResponse.nickname()).isEqualTo("스터디장");

      MemberResponse memberResponse = result.members().stream()
          .filter(m -> !m.isOwner())
          .findFirst()
          .orElseThrow();
      assertThat(memberResponse.memberId()).isEqualTo(memberId);
      assertThat(memberResponse.nickname()).isEqualTo("멤버1");
    }

    @Test
    @DisplayName("활성 멤버가 없으면 빈 목록을 반환한다")
    void should_return_empty_list_when_no_active_members() {
      // given
      given(studyMemberRepository.findAllByStudyIdAndStudyMemberStatusIn(studyId, ACTIVE_STATUSES))
          .willReturn(List.of());
      given(getMemberInfoPort.getMemberInfo(List.of()))
          .willReturn(Map.of());

      // when
      GetStudyMembersResponse result = sut.getStudyMembers(studyId);

      // then
      assertThat(result.totalMembers()).isZero();
      assertThat(result.members()).isEmpty();
    }

    @Test
    @DisplayName("스터디장만 있는 경우에도 정상적으로 반환한다")
    void should_return_only_owner_when_no_other_members() {
      // given
      long ownerId = 10L;
      StudyMember owner = StudyMember.create(1L, studyId, ownerId);
      MemberInfoResponse ownerInfo = MemberInfoResponse.of("스터디장", "https://image.url/owner.jpg");

      given(studyMemberRepository.findAllByStudyIdAndStudyMemberStatusIn(studyId, ACTIVE_STATUSES))
          .willReturn(List.of(owner));
      given(getMemberInfoPort.getMemberInfo(List.of(ownerId)))
          .willReturn(Map.of(ownerId, ownerInfo));

      // when
      GetStudyMembersResponse result = sut.getStudyMembers(studyId);

      // then
      assertThat(result.totalMembers()).isEqualTo(1);
      assertThat(result.members()).hasSize(1);
      assertThat(result.members().get(0).isOwner()).isTrue();
    }

    private StudyMember createApprovedMember(long id, long studyId, long memberId) {
      StudyMember member = StudyMember.apply(id, studyId, memberId, "가입 신청");
      ReflectionTestUtils.setField(member, "studyMemberStatus", StudyMemberStatus.APPROVED);
      return member;
    }
  }
}
