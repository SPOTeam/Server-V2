package kr.spot.application.query;

import static kr.spot.common.StudyFixture.createStudies;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import kr.spot.ports.GetPreferredCategoryPort;
import kr.spot.ports.GetPreferredRegionPort;
import kr.spot.study.application.query.GetMyStudyInfoService;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.enums.Category;
import kr.spot.study.domain.enums.FeeCategory;
import kr.spot.study.domain.enums.RecruitingStatus;
import kr.spot.study.domain.enums.SortBy;
import kr.spot.study.domain.enums.StudyMemberStatus;
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

      given(studyQueryRepository.findMyStudies(viewerId, status, null, pageSize + 1))
          .willReturn(studies);

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyStudyOverview(viewerId, status,
          null,
          pageSize);

      // then
      verify(studyQueryRepository).findMyStudies(viewerId, status, null, pageSize + 1);
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

      given(studyQueryRepository.findMyStudies(viewerId, status, null, pageSize + 1))
          .willReturn(studies);

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyStudyOverview(viewerId, status,
          null,
          pageSize);

      // then
      verify(studyQueryRepository).findMyStudies(viewerId, status, null, pageSize + 1);
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

      given(studyQueryRepository.findMyStudies(viewerId, status, cursor, pageSize + 1))
          .willReturn(studies);

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyStudyOverview(viewerId, status,
          cursor,
          pageSize);

      // then
      verify(studyQueryRepository).findMyStudies(viewerId, status, cursor, pageSize + 1);
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

      given(studyQueryRepository.findMyStudies(viewerId, status, null, maxPageSize + 1))
          .willReturn(studies);

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyStudyOverview(viewerId, status,
          null,
          requestedSize);

      // then
      verify(studyQueryRepository).findMyStudies(viewerId, status, null, maxPageSize + 1);
      assertThat(response.content()).hasSize(maxPageSize);
      assertThat(response.hasNext()).isTrue();
    }
  }

  @Nested
  @DisplayName("내 관심 지역 스터디 목록 조회 (getMyPreferredRegionStudies)")
  class GetMyPreferredRegionStudies {

    private final Long viewerId = 1L;
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
          anyInt(),
          eq(preferredRegions)))
          .willReturn(studies);

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyPreferredRegionStudies(
          viewerId, RecruitingStatus.RECRUITING, FeeCategory.ABOVE_50K, Collections.emptyList(),
          SortBy.HITS,
          null, pageSize, Collections.emptyList()
      );

      // then
      verify(getPreferredRegionPort).get(viewerId);
      verify(studyQueryRepository).findMyPreferredRegionStudies(
          eq(RecruitingStatus.RECRUITING), eq(FeeCategory.ABOVE_50K), eq(Collections.emptyList()),
          eq(SortBy.HITS),
          eq(null), eq(pageSize + 1), eq(preferredRegions)
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
          anyInt(),
          eq(expectedRegions)))
          .willReturn(studies);

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyPreferredRegionStudies(
          viewerId, RecruitingStatus.RECRUITING, FeeCategory.ABOVE_50K, Collections.emptyList(),
          SortBy.HITS,
          null, pageSize, filterRegions
      );

      // then
      verify(getPreferredRegionPort).get(viewerId);
      verify(studyQueryRepository).findMyPreferredRegionStudies(
          any(), any(), any(), any(), any(), eq(pageSize + 1), eq(expectedRegions)
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
          anyInt(),
          eq(Collections.emptyList())))
          .willReturn(Collections.emptyList());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyPreferredRegionStudies(
          viewerId, RecruitingStatus.RECRUITING, FeeCategory.ABOVE_50K, Collections.emptyList(),
          SortBy.HITS,
          null, pageSize, Collections.emptyList()
      );

      // then
      verify(getPreferredRegionPort).get(viewerId);
      verify(studyQueryRepository).findMyPreferredRegionStudies(
          any(), any(), any(), any(), any(), eq(pageSize + 1), eq(Collections.emptyList())
      );
      assertThat(response.content()).isEmpty();
      assertThat(response.hasNext()).isFalse();
      assertThat(response.nextCursor()).isNull();
    }
  }

  @Nested
  @DisplayName("내 관심 카테고리 스터디 목록 조회 (getMyPreferredCategoryStudies)")
  class GetMyPreferredCategoryStudies {

    private final Long viewerId = 1L;
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
      given(
          studyQueryRepository.findMyPreferredCategoryStudies(any(), any(), any(), any(), anyInt(),
              eq(preferredCategory)))
          .willReturn(studies);

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyPreferredCategoryStudies(
          viewerId, null, RecruitingStatus.RECRUITING, FeeCategory.ABOVE_50K,
          SortBy.HITS, null, pageSize
      );

      // then
      verify(getPreferredCategoryPort).get(viewerId);
      verify(studyQueryRepository).findMyPreferredCategoryStudies(
          eq(RecruitingStatus.RECRUITING), eq(FeeCategory.ABOVE_50K), eq(SortBy.HITS),
          eq(null), eq(pageSize + 1),
          eq(List.of(Category.SELF_STUDY, Category.CAREER))
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
      given(
          studyQueryRepository.findMyPreferredCategoryStudies(any(), any(), any(), any(), anyInt(),
              eq(Collections.emptyList())))
          .willReturn(Collections.emptyList());

      // when
      GetStudyOverviewResponse response = getMyStudyInfoService.getMyPreferredCategoryStudies(
          viewerId, null, RecruitingStatus.RECRUITING, FeeCategory.ABOVE_50K,
          SortBy.HITS, null, pageSize
      );

      // then
      verify(getPreferredCategoryPort).get(viewerId);
      verify(studyQueryRepository).findMyPreferredCategoryStudies(
          any(), any(), any(), any(), eq(pageSize + 1), eq(Collections.emptyList())
      );
      assertThat(response.content()).isEmpty();
      assertThat(response.hasNext()).isFalse();
      assertThat(response.nextCursor()).isNull();
    }
  }
}
