package kr.spot.study.application.query;

import static kr.spot.study.common.StudyFixture.createStudies;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import kr.spot.ports.GetPreferredCategoryPort;
import kr.spot.ports.GetPreferredRegionPort;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.enums.Category;
import kr.spot.study.domain.enums.FeeCategory;
import kr.spot.study.domain.enums.RecruitingStatus;
import kr.spot.study.domain.enums.SortBy;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.infrastructure.jpa.associations.StudyLikeRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.study.infrastructure.jpa.querydsl.StudyQueryRepository;
import kr.spot.study.presentation.query.dto.response.GetStudyOverviewResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMyStudyInfoService 단위 테스트")
class GetMyStudyInfoServiceTest {

  @InjectMocks
  private GetMyStudyInfoService getMyStudyInfoService;
  @Mock
  private GetPreferredRegionPort getPreferredRegionPort;
  @Mock
  private GetPreferredCategoryPort getPreferredCategoryPort;
  @Mock
  private StudyQueryRepository studyQueryRepository;
  @Mock
  private StudyLikeRepository studyLikeRepository;
  @Mock
  private StudyMemberRepository studyMemberRepository;

  @Nested
  @DisplayName("내 스터디 목록 조회 (getMyStudyOverview)")
  class GetMyStudyOverview {

    private final Long viewerId = 1L;
    private final StudyMemberStatus status = StudyMemberStatus.APPROVED;

    @Test
    @DisplayName("첫 페이지 조회 시, 다음 페이지가 있으면 hasNext=true 와 nextCursor를 반환한다")
    void should_return_first_page_with_next_page() {
      // given
      int pageSize = 10;
      List<Study> studies = createStudies(pageSize + 1); // 11개 생성
      Long expectedNextCursor = studies.get(pageSize - 1).getId(); // 10번째 스터디의 ID

      given(studyQueryRepository.findMyStudies(viewerId, List.of(status), null, pageSize + 1))
          .willReturn(studies);
      given(studyQueryRepository.countMyStudies(viewerId, List.of(status))).willReturn(
          (long) studies.size());
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());

      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyStudyOverview(viewerId,
          List.of(status),
          null,
          pageSize);

      // then
      verify(studyQueryRepository).findMyStudies(viewerId, List.of(status), null, pageSize + 1);
      assertThat(response.content()).hasSize(pageSize);
      assertThat(response.hasNext()).isTrue();
      assertThat(response.nextCursor()).isEqualTo(expectedNextCursor);
    }

    @Test
    @DisplayName("마지막 페이지 조회 시, hasNext=false 와 nextCursor=null을 반환한다")
    void should_return_last_page() {
      // given
      int pageSize = 10;
      List<Study> studies = createStudies(5); // 5개 생성 (pageSize보다 적음)

      given(studyQueryRepository.findMyStudies(viewerId, List.of(status), null, pageSize + 1))
          .willReturn(studies);
      given(studyQueryRepository.countMyStudies(viewerId, List.of(status))).willReturn(
          (long) studies.size());
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());
      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyStudyOverview(viewerId,
          List.of(status),
          null,
          pageSize);

      // then
      verify(studyQueryRepository).findMyStudies(viewerId, List.of(status), null, pageSize + 1);
      assertThat(response.content()).hasSize(5);
      assertThat(response.hasNext()).isFalse();
      assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("커서 기반으로 다음 페이지를 정상적으로 조회한다")
    void should_return_next_page_with_cursor() {
      // given
      int pageSize = 10;
      Long cursor = 11L; // 이전 페이지의 마지막 스터디 ID
      List<Study> studies = createStudies(pageSize + 1);
      Long expectedNextCursor = studies.get(pageSize - 1).getId();

      given(studyQueryRepository.findMyStudies(viewerId, List.of(status), cursor, pageSize + 1))
          .willReturn(studies);
      given(studyQueryRepository.countMyStudies(viewerId, List.of(status))).willReturn(
          (long) studies.size());
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyStudyOverview(viewerId,
          List.of(status),
          cursor,
          pageSize);

      // then
      verify(studyQueryRepository).findMyStudies(viewerId, List.of(status), cursor, pageSize + 1);
      assertThat(response.content()).hasSize(pageSize);
      assertThat(response.hasNext()).isTrue();
      assertThat(response.nextCursor()).isEqualTo(expectedNextCursor);
    }

    @Test
    @DisplayName("요청 size가 MAX_PAGE_SIZE를 초과하면 MAX_PAGE_SIZE로 제한된다")
    void should_cap_page_size_at_max_page_size() {
      // given
      int requestedSize = 100;
      int maxPageSize = GetMyStudyInfoService.MAX_PAGE_SIZE; // 50
      List<Study> studies = createStudies(maxPageSize + 1); // 51개 생성

      given(studyQueryRepository.findMyStudies(viewerId, List.of(status), null, maxPageSize + 1))
          .willReturn(studies);
      given(studyQueryRepository.countMyStudies(viewerId, List.of(status))).willReturn(
          (long) studies.size());
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyStudyOverview(viewerId,
          List.of(status),
          null,
          requestedSize);

      // then
      verify(studyQueryRepository).findMyStudies(viewerId, List.of(status), null, maxPageSize + 1);
      assertThat(response.content()).hasSize(maxPageSize);
      assertThat(response.hasNext()).isTrue();
    }
  }

  @Nested
  @DisplayName("내 관심 지역 스터디 목록 조회 (getMyPreferredRegionStudies)")
  class GetMyPreferredRegionStudies {

    private final long viewerId = 1L;
    private final int pageSize = 10;

    @Test
    @DisplayName("선호 지역이 있고 필터가 없으면, 모든 선호 지역의 스터디를 조회한다")
    void should_find_studies_in_all_preferred_regions_when_no_filter() {
      // given
      List<String> preferredRegions = List.of("11000", "12000");
      List<Study> studies = createStudies(pageSize + 1);
      Long expectedNextCursor = studies.get(pageSize - 1).getId();

      given(getPreferredRegionPort.get(viewerId)).willReturn(preferredRegions);
      given(studyQueryRepository.findMyPreferredRegionStudies(any(), any(), any(), any(), any(),
          any(), anyInt(), eq(preferredRegions)))
          .willReturn(studies);
      given(studyQueryRepository.countMyPreferredRegionStudies(any(), any(), any(), any(),
          eq(preferredRegions)))
          .willReturn((long) studies.size());
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyPreferredRegionStudies(
          viewerId, RecruitingStatus.RECRUITING, FeeCategory.ABOVE_50K, Collections.emptyList(),
          null, SortBy.HITS, null, pageSize, Collections.emptyList()
      );

      // then
      verify(getPreferredRegionPort).get(viewerId);
      verify(studyQueryRepository).findMyPreferredRegionStudies(
          eq(RecruitingStatus.RECRUITING), eq(FeeCategory.ABOVE_50K), eq(Collections.emptyList()),
          eq(null), eq(SortBy.HITS), eq(null), eq(pageSize + 1), eq(preferredRegions)
      );
      assertThat(response.content()).hasSize(pageSize);
      assertThat(response.hasNext()).isTrue();
      assertThat(response.nextCursor()).isEqualTo(expectedNextCursor);
    }

    @Test
    @DisplayName("선호 지역과 필터가 모두 있으면, 교집합에 해당하는 지역의 스터디만 조회한다")
    void should_find_studies_in_filtered_preferred_regions() {
      // given
      List<String> preferredRegions = List.of("11000", "12000", "13000");
      List<String> filterRegions = List.of("12000", "13000", "14000");
      List<String> expectedRegions = List.of("12000", "13000");
      List<Study> studies = createStudies(5);

      given(getPreferredRegionPort.get(viewerId)).willReturn(preferredRegions);
      given(studyQueryRepository.findMyPreferredRegionStudies(any(), any(), any(), any(), any(),
          any(), anyInt(), eq(expectedRegions)))
          .willReturn(studies);
      given(studyQueryRepository.countMyPreferredRegionStudies(any(), any(), any(), any(),
          eq(expectedRegions)))
          .willReturn((long) studies.size());
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyPreferredRegionStudies(
          viewerId, RecruitingStatus.RECRUITING, FeeCategory.ABOVE_50K, Collections.emptyList(),
          null, SortBy.HITS, null, pageSize, filterRegions
      );

      // then
      verify(getPreferredRegionPort).get(viewerId);
      verify(studyQueryRepository).findMyPreferredRegionStudies(
          any(), any(), any(), any(), any(), any(), eq(pageSize + 1), eq(expectedRegions)
      );
      assertThat(response.content()).hasSize(5);
      assertThat(response.hasNext()).isFalse();
    }

    @Test
    @DisplayName("선호 지역이 없으면, 빈 결과를 반환한다")
    void should_return_empty_when_no_preferred_regions() {
      // given
      given(getPreferredRegionPort.get(viewerId)).willReturn(Collections.emptyList());
      given(studyQueryRepository.findMyPreferredRegionStudies(any(), any(), any(), any(), any(),
          any(), anyInt(), eq(Collections.emptyList())))
          .willReturn(Collections.emptyList());
      given(studyQueryRepository.countMyPreferredRegionStudies(any(), any(), any(), any(),
          eq(Collections.emptyList())))
          .willReturn(0L);
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyPreferredRegionStudies(
          viewerId, RecruitingStatus.RECRUITING, FeeCategory.ABOVE_50K, Collections.emptyList(),
          null, SortBy.HITS, null, pageSize, Collections.emptyList()
      );

      // then
      verify(getPreferredRegionPort).get(viewerId);
      verify(studyQueryRepository).findMyPreferredRegionStudies(
          any(), any(), any(), any(), any(), any(), eq(pageSize + 1), eq(Collections.emptyList())
      );
      assertThat(response.content()).isEmpty();
      assertThat(response.hasNext()).isFalse();
      assertThat(response.nextCursor()).isNull();
    }
  }

  @Nested
  @DisplayName("내 관심 카테고리 스터디 목록 조회 (getMyPreferredCategoryStudies)")
  class GetMyPreferredCategoryStudies {

    private final long viewerId = 1L;
    private final int pageSize = 10;

    @Test
    @DisplayName("선호 카테고리가 있고 필터가 없으면, 모든 선호 카테고리의 스터디를 조회한다")
    void should_find_studies_in_all_preferred_regions_when_no_filter() {
      // given
      List<String> preferredCategoryName = List.of("SELF_STUDY", "CAREER");
      List<Category> preferredCategory = List.of(Category.SELF_STUDY, Category.CAREER);
      List<Study> studies = createStudies(pageSize + 1);
      Long expectedNextCursor = studies.get(pageSize - 1).getId();

      given(getPreferredCategoryPort.get(viewerId)).willReturn(preferredCategoryName);
      given(studyQueryRepository.findMyPreferredCategoryStudies(any(), any(), any(), any(), any(),
          anyInt(), eq(preferredCategory)))
          .willReturn(studies);
      given(studyQueryRepository.countMyPreferredCategoryStudies(any(), any(), any(),
          eq(preferredCategory)))
          .willReturn((long) studies.size());
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyPreferredCategoryStudies(
          viewerId, null, RecruitingStatus.RECRUITING, FeeCategory.ABOVE_50K,
          null, SortBy.HITS, null, pageSize
      );

      // then
      verify(getPreferredCategoryPort).get(viewerId);
      verify(studyQueryRepository).findMyPreferredCategoryStudies(
          eq(RecruitingStatus.RECRUITING), eq(FeeCategory.ABOVE_50K), eq(null), eq(SortBy.HITS),
          eq(null), eq(pageSize + 1), eq(preferredCategory)
      );
      assertThat(response.content()).hasSize(pageSize);
      assertThat(response.hasNext()).isTrue();
      assertThat(response.nextCursor()).isEqualTo(expectedNextCursor);
    }


    @Test
    @DisplayName("선호 카테고리가 없으면, 빈 결과를 반환한다")
    void should_return_empty_when_no_preferred_regions() {
      // given
      given(getPreferredCategoryPort.get(viewerId)).willReturn(Collections.emptyList());
      given(studyQueryRepository.findMyPreferredCategoryStudies(any(), any(), any(), any(), any(),
          anyInt(), eq(Collections.emptyList())))
          .willReturn(Collections.emptyList());
      given(studyQueryRepository.countMyPreferredCategoryStudies(any(), any(), any(),
          eq(Collections.emptyList())))
          .willReturn(0L);
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyPreferredCategoryStudies(
          viewerId, null, RecruitingStatus.RECRUITING, FeeCategory.ABOVE_50K,
          null, SortBy.HITS, null, pageSize
      );

      // then
      verify(getPreferredCategoryPort).get(viewerId);
      verify(studyQueryRepository).findMyPreferredCategoryStudies(
          any(), any(), any(), any(), any(), eq(pageSize + 1), eq(Collections.emptyList())
      );
      assertThat(response.content()).isEmpty();
      assertThat(response.hasNext()).isFalse();
      assertThat(response.nextCursor()).isNull();
    }
  }

  @Nested
  @DisplayName("추천 스터디 조회 (getRecommendedStudies)")
  class GetRecommendedStudies {

    private final long memberId = 1L;

    @Test
    @DisplayName("선호 카테고리 스터디가 3개 이상이면, 인기 스터디를 조회하지 않는다")
    void should_not_fetch_popular_when_enough_preferred_studies() {
      // given
      List<String> preferredCategoryNames = List.of("SELF_STUDY", "CAREER");
      List<Category> preferredCategories = List.of(Category.SELF_STUDY, Category.CAREER);
      List<Study> candidates = createStudies(10);

      given(getPreferredCategoryPort.get(memberId)).willReturn(preferredCategoryNames);
      given(studyQueryRepository.findRecruitingStudiesByCategories(preferredCategories, 20))
          .willReturn(candidates);
      given(studyLikeRepository.findStudyIdsByMemberId(memberId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(memberId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getRecommendedStudies(memberId);

      // then
      verify(getPreferredCategoryPort).get(memberId);
      verify(studyQueryRepository).findRecruitingStudiesByCategories(preferredCategories, 20);
      verify(studyQueryRepository, never()).findPopularRecruitingStudies(any(), anyInt());
      assertThat(response.content()).hasSize(3);
      assertThat(response.totalElements()).isEqualTo(3L);
    }

    @Test
    @DisplayName("선호 카테고리 스터디가 3개 미만이면, 인기 스터디로 부족한 만큼 채운다")
    void should_fill_with_popular_studies_when_not_enough_preferred() {
      // given
      List<String> preferredCategoryNames = List.of("SELF_STUDY");
      List<Category> preferredCategories = List.of(Category.SELF_STUDY);
      List<Study> preferredStudies = createStudies(1);
      List<Study> popularStudies = createStudies(2);

      given(getPreferredCategoryPort.get(memberId)).willReturn(preferredCategoryNames);
      given(studyQueryRepository.findRecruitingStudiesByCategories(preferredCategories, 20))
          .willReturn(preferredStudies);
      given(studyQueryRepository.findPopularRecruitingStudies(
          List.of(preferredStudies.getFirst().getId()), 2))
          .willReturn(popularStudies);
      given(studyLikeRepository.findStudyIdsByMemberId(memberId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(memberId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getRecommendedStudies(memberId);

      // then
      verify(studyQueryRepository).findRecruitingStudiesByCategories(preferredCategories, 20);
      verify(studyQueryRepository).findPopularRecruitingStudies(
          List.of(preferredStudies.getFirst().getId()), 2);
      assertThat(response.content()).hasSize(3);
      assertThat(response.totalElements()).isEqualTo(3L);
    }

    @Test
    @DisplayName("선호 카테고리가 없으면, 인기 스터디 3개를 반환한다")
    void should_return_popular_studies_when_no_preferred_categories() {
      // given
      List<Study> popularStudies = createStudies(3);

      given(getPreferredCategoryPort.get(memberId)).willReturn(Collections.emptyList());
      given(studyQueryRepository.findPopularRecruitingStudies(Collections.emptyList(), 3))
          .willReturn(popularStudies);
      given(studyLikeRepository.findStudyIdsByMemberId(memberId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(memberId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getRecommendedStudies(memberId);

      // then
      verify(getPreferredCategoryPort).get(memberId);
      verify(studyQueryRepository).findPopularRecruitingStudies(Collections.emptyList(), 3);
      assertThat(response.content()).hasSize(3);
      assertThat(response.totalElements()).isEqualTo(3L);
    }

    @Test
    @DisplayName("선호 카테고리 스터디와 인기 스터디 모두 없으면, 빈 결과를 반환한다")
    void should_return_empty_when_no_studies_at_all() {
      // given
      List<String> preferredCategoryNames = List.of("SELF_STUDY");
      List<Category> preferredCategories = List.of(Category.SELF_STUDY);

      given(getPreferredCategoryPort.get(memberId)).willReturn(preferredCategoryNames);
      given(studyQueryRepository.findRecruitingStudiesByCategories(preferredCategories, 20))
          .willReturn(Collections.emptyList());
      given(studyQueryRepository.findPopularRecruitingStudies(Collections.emptyList(), 3))
          .willReturn(Collections.emptyList());
      given(studyLikeRepository.findStudyIdsByMemberId(memberId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(memberId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getRecommendedStudies(memberId);

      // then
      verify(studyQueryRepository).findRecruitingStudiesByCategories(preferredCategories, 20);
      verify(studyQueryRepository).findPopularRecruitingStudies(Collections.emptyList(), 3);
      assertThat(response.content()).isEmpty();
      assertThat(response.totalElements()).isEqualTo(0L);
    }
  }

  @Nested
  @DisplayName("모집중 스터디 조회 (getRecruitingStudies)")
  class GetRecruitingStudies {

    private final long viewerId = 1L;
    private final int pageSize = 10;

    @Test
    @DisplayName("필터 조건에 맞는 모집중 스터디를 조회한다")
    void should_find_recruiting_studies_with_filters() {
      // given
      List<Study> studies = createStudies(pageSize + 1);
      Long expectedNextCursor = studies.get(pageSize - 1).getId();

      given(studyQueryRepository.findRecruitingStudies(
          eq(FeeCategory.NONE), eq(List.of(Category.LANGUAGE)), eq(true), eq(SortBy.HITS),
          eq(null), eq(pageSize + 1)))
          .willReturn(studies);
      given(studyQueryRepository.countRecruitingStudies(
          eq(FeeCategory.NONE), eq(List.of(Category.LANGUAGE)), eq(true)))
          .willReturn((long) studies.size());
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getRecruitingStudies(
          viewerId, FeeCategory.NONE, List.of(Category.LANGUAGE), true, SortBy.HITS, null, pageSize
      );

      // then
      verify(studyQueryRepository).findRecruitingStudies(
          eq(FeeCategory.NONE), eq(List.of(Category.LANGUAGE)), eq(true), eq(SortBy.HITS),
          eq(null), eq(pageSize + 1));
      assertThat(response.content()).hasSize(pageSize);
      assertThat(response.hasNext()).isTrue();
      assertThat(response.nextCursor()).isEqualTo(expectedNextCursor);
    }

    @Test
    @DisplayName("필터 없이 모집중 스터디를 조회한다")
    void should_find_recruiting_studies_without_filters() {
      // given
      List<Study> studies = createStudies(5);

      given(studyQueryRepository.findRecruitingStudies(
          eq(null), eq(null), eq(null), eq(null), eq(null), eq(pageSize + 1)))
          .willReturn(studies);
      given(studyQueryRepository.countRecruitingStudies(eq(null), eq(null), eq(null)))
          .willReturn((long) studies.size());
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getRecruitingStudies(
          viewerId, null, null, null, null, null, pageSize
      );

      // then
      assertThat(response.content()).hasSize(5);
      assertThat(response.hasNext()).isFalse();
      assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("좋아요한 스터디는 isLiked가 true로 표시된다")
    void should_mark_liked_studies() {
      // given
      List<Study> studies = createStudies(3);
      Long likedStudyId = studies.get(0).getId();

      given(studyQueryRepository.findRecruitingStudies(any(), any(), any(), any(), any(), anyInt()))
          .willReturn(studies);
      given(studyQueryRepository.countRecruitingStudies(any(), any(), any()))
          .willReturn((long) studies.size());
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of(likedStudyId));
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getRecruitingStudies(
          viewerId, null, null, null, null, null, pageSize
      );

      // then
      assertThat(response.content().get(0).isLiked()).isTrue();
      assertThat(response.content().get(1).isLiked()).isFalse();
      assertThat(response.content().get(2).isLiked()).isFalse();
    }
  }

  @Nested
  @DisplayName("카테고리별 스터디 조회 (getStudiesByCategory)")
  class GetStudiesByCategory {

    private final long viewerId = 1L;
    private final int pageSize = 10;

    @Test
    @DisplayName("특정 카테고리의 스터디를 조회한다")
    void should_find_studies_by_category() {
      // given
      List<Study> studies = createStudies(pageSize + 1);
      Long expectedNextCursor = studies.get(pageSize - 1).getId();

      given(studyQueryRepository.findStudiesByCategory(
          eq(RecruitingStatus.RECRUITING), eq(FeeCategory.NONE), eq(Category.LANGUAGE),
          eq(false), eq(SortBy.LIKES), eq(null), eq(pageSize + 1)))
          .willReturn(studies);
      given(studyQueryRepository.countStudiesByCategory(
          eq(RecruitingStatus.RECRUITING), eq(FeeCategory.NONE), eq(Category.LANGUAGE), eq(false)))
          .willReturn((long) studies.size());
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getStudiesByCategory(
          viewerId, RecruitingStatus.RECRUITING, FeeCategory.NONE, Category.LANGUAGE,
          false, SortBy.LIKES, null, pageSize
      );

      // then
      verify(studyQueryRepository).findStudiesByCategory(
          eq(RecruitingStatus.RECRUITING), eq(FeeCategory.NONE), eq(Category.LANGUAGE),
          eq(false), eq(SortBy.LIKES), eq(null), eq(pageSize + 1));
      assertThat(response.content()).hasSize(pageSize);
      assertThat(response.hasNext()).isTrue();
      assertThat(response.nextCursor()).isEqualTo(expectedNextCursor);
    }

    @Test
    @DisplayName("조건에 맞는 스터디가 없으면 빈 결과를 반환한다")
    void should_return_empty_when_no_studies() {
      // given
      given(studyQueryRepository.findStudiesByCategory(any(), any(), any(), any(), any(), any(),
          anyInt()))
          .willReturn(Collections.emptyList());
      given(studyQueryRepository.countStudiesByCategory(any(), any(), any(), any()))
          .willReturn(0L);
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getStudiesByCategory(
          viewerId, RecruitingStatus.COMPLETED, FeeCategory.ABOVE_50K, Category.CAREER,
          true, SortBy.RECENT, null, pageSize
      );

      // then
      assertThat(response.content()).isEmpty();
      assertThat(response.hasNext()).isFalse();
      assertThat(response.nextCursor()).isNull();
      assertThat(response.totalElements()).isEqualTo(0L);
    }
  }

  @Nested
  @DisplayName("좋아요한 스터디 조회 (getLikedStudies)")
  class GetLikedStudies {

    private final long viewerId = 1L;
    private final int pageSize = 10;

    @Test
    @DisplayName("내가 좋아요한 스터디 목록을 조회한다")
    void should_find_liked_studies() {
      // given
      List<Study> studies = createStudies(pageSize + 1);
      Long expectedNextCursor = studies.get(pageSize - 1).getId();
      Set<Long> likedStudyIds = studies.stream().map(Study::getId)
          .collect(java.util.stream.Collectors.toSet());

      given(studyQueryRepository.findLikedStudies(viewerId, null, pageSize + 1))
          .willReturn(studies);
      given(studyQueryRepository.countLikedStudies(viewerId))
          .willReturn((long) studies.size());
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(likedStudyIds);
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getLikedStudies(viewerId, null,
          pageSize);

      // then
      verify(studyQueryRepository).findLikedStudies(viewerId, null, pageSize + 1);
      assertThat(response.content()).hasSize(pageSize);
      assertThat(response.hasNext()).isTrue();
      assertThat(response.nextCursor()).isEqualTo(expectedNextCursor);
      assertThat(response.content()).allMatch(study -> study.isLiked());
    }

    @Test
    @DisplayName("좋아요한 스터디가 없으면 빈 결과를 반환한다")
    void should_return_empty_when_no_liked_studies() {
      // given
      given(studyQueryRepository.findLikedStudies(viewerId, null, pageSize + 1))
          .willReturn(Collections.emptyList());
      given(studyQueryRepository.countLikedStudies(viewerId))
          .willReturn(0L);
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getLikedStudies(viewerId, null,
          pageSize);

      // then
      assertThat(response.content()).isEmpty();
      assertThat(response.hasNext()).isFalse();
      assertThat(response.nextCursor()).isNull();
      assertThat(response.totalElements()).isEqualTo(0L);
    }

    @Test
    @DisplayName("커서 기반으로 다음 페이지를 조회한다")
    void should_find_next_page_with_cursor() {
      // given
      Long cursor = 100L;
      List<Study> studies = createStudies(5);

      given(studyQueryRepository.findLikedStudies(viewerId, cursor, pageSize + 1))
          .willReturn(studies);
      given(studyQueryRepository.countLikedStudies(viewerId))
          .willReturn(15L);
      given(studyLikeRepository.findStudyIdsByMemberId(viewerId)).willReturn(Set.of());
      given(studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(viewerId,
          StudyMemberStatus.OWNER)).willReturn(Set.of());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getLikedStudies(viewerId, cursor,
          pageSize);

      // then
      verify(studyQueryRepository).findLikedStudies(viewerId, cursor, pageSize + 1);
      assertThat(response.content()).hasSize(5);
      assertThat(response.hasNext()).isFalse();
      assertThat(response.totalElements()).isEqualTo(15L);
    }
  }
}
