package kr.spot.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import kr.spot.common.fixture.MemberFixture;
import kr.spot.domain.Member;
import kr.spot.domain.association.PreferredCategory;
import kr.spot.domain.association.PreferredRegion;
import kr.spot.domain.enums.LoginType;
import kr.spot.infrastructure.jpa.MemberRepository;
import kr.spot.infrastructure.jpa.PreferredCategoryRepository;
import kr.spot.infrastructure.jpa.PreferredRegionRepository;
import kr.spot.ports.StudyParticipationInfoPort;
import kr.spot.ports.dto.StudyParticipationInfo;
import kr.spot.presentation.query.dto.response.GetMemberInfoResponse;
import kr.spot.presentation.query.dto.response.GetMemberNameResponse;
import kr.spot.presentation.query.dto.response.GetMemberPreferCategoryResponse;
import kr.spot.presentation.query.dto.response.GetMemberPreferRegionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetMemberInfoServiceTest {

  @Mock
  MemberRepository memberRepository;

  @Mock
  PreferredCategoryRepository preferredCategoryRepository;

  @Mock
  PreferredRegionRepository preferredRegionRepository;

  @Mock
  StudyParticipationInfoPort studyParticipationInfoPort;

  @InjectMocks
  GetMemberInfoService sut;

  @Nested
  @DisplayName("getMemberName 메서드는")
  class GetMemberNameTest {

    @Test
    @DisplayName("회원 ID로 회원 이름을 조회한다")
    void should_return_member_name_when_member_exists() {
      // given
      long memberId = MemberFixture.ID;
      Member member = MemberFixture.member();

      when(memberRepository.getMemberById(memberId)).thenReturn(member);

      // when
      GetMemberNameResponse response = sut.getMemberName(memberId);

      // then
      assertThat(response.name()).isEqualTo(MemberFixture.NAME);
    }
  }

  @Nested
  @DisplayName("getMemberInfo 메서드는")
  class GetMemberInfoTest {

    @Test
    @DisplayName("회원 ID로 회원 정보와 스터디 참여 정보를 조회한다")
    void should_return_member_info_with_study_participation_info() {
      // given
      long memberId = MemberFixture.ID;
      Member member = MemberFixture.member();
      StudyParticipationInfo studyParticipationInfo = StudyParticipationInfo.from(3L, 1L, 2L);

      when(memberRepository.getMemberById(memberId)).thenReturn(member);
      when(studyParticipationInfoPort.getStudyParticipationInfo(memberId))
          .thenReturn(studyParticipationInfo);

      // when
      GetMemberInfoResponse response = sut.getMemberInfo(memberId);

      // then
      assertThat(response.memberId()).isEqualTo(MemberFixture.ID);
      assertThat(response.nickname()).isEqualTo(MemberFixture.NAME);
      assertThat(response.profileImageUrl()).isEqualTo(MemberFixture.PROFILE_IMAGE);
      assertThat(response.loginType()).isEqualTo(LoginType.KAKAO);
      assertThat(response.email()).isEqualTo(MemberFixture.EMAIL);
      assertThat(response.studyParticipationInfo().participatingStudyCount()).isEqualTo(3L);
      assertThat(response.studyParticipationInfo().recruitingStudyCount()).isEqualTo(1L);
      assertThat(response.studyParticipationInfo().appliedStudyCount()).isEqualTo(2L);
    }
  }

  @Nested
  @DisplayName("getMemberPreferCategories 메서드는")
  class GetMemberPreferCategoriesTest {

    @Test
    @DisplayName("회원 ID로 선호 카테고리 목록을 조회한다")
    void should_return_preferred_categories_when_member_has_categories() {
      // given
      long memberId = MemberFixture.ID;
      List<PreferredCategory> preferredCategories = List.of(
          PreferredCategory.of(1L, memberId, "PROGRAMMING"),
          PreferredCategory.of(2L, memberId, "DESIGN"),
          PreferredCategory.of(3L, memberId, "LANGUAGE")
      );

      when(preferredCategoryRepository.findAllByMemberId(memberId))
          .thenReturn(preferredCategories);

      // when
      GetMemberPreferCategoryResponse response = sut.getMemberPreferCategories(memberId);

      // then
      assertThat(response.categories()).containsExactly("PROGRAMMING", "DESIGN", "LANGUAGE");
      assertThat(response.totalCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("선호 카테고리가 없으면 빈 목록을 반환한다")
    void should_return_empty_list_when_member_has_no_categories() {
      // given
      long memberId = MemberFixture.ID;

      when(preferredCategoryRepository.findAllByMemberId(memberId))
          .thenReturn(List.of());

      // when
      GetMemberPreferCategoryResponse response = sut.getMemberPreferCategories(memberId);

      // then
      assertThat(response.categories()).isEmpty();
      assertThat(response.totalCount()).isZero();
    }
  }

  @Nested
  @DisplayName("getMemberPreferRegions 메서드는")
  class GetMemberPreferRegionsTest {

    @Test
    @DisplayName("회원 ID로 선호 지역 목록을 조회한다")
    void should_return_preferred_regions_when_member_has_regions() {
      // given
      long memberId = MemberFixture.ID;
      List<PreferredRegion> preferredRegions = List.of(
          PreferredRegion.of(1L, memberId, "SEOUL"),
          PreferredRegion.of(2L, memberId, "BUSAN")
      );

      when(preferredRegionRepository.findAllByMemberId(memberId))
          .thenReturn(preferredRegions);

      // when
      GetMemberPreferRegionResponse response = sut.getMemberPreferRegions(memberId);

      // then
      assertThat(response.regionCodes()).containsExactly("SEOUL", "BUSAN");
      assertThat(response.totalCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("선호 지역이 없으면 빈 목록을 반환한다")
    void should_return_empty_list_when_member_has_no_regions() {
      // given
      long memberId = MemberFixture.ID;

      when(preferredRegionRepository.findAllByMemberId(memberId))
          .thenReturn(List.of());

      // when
      GetMemberPreferRegionResponse response = sut.getMemberPreferRegions(memberId);

      // then
      assertThat(response.regionCodes()).isEmpty();
      assertThat(response.totalCount()).isZero();
    }
  }
}
