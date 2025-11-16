package kr.spot.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import kr.spot.common.StudyFixture;
import kr.spot.domain.Study;
import kr.spot.domain.enums.StudyMemberStatus;
import kr.spot.domain.vo.Fee;
import kr.spot.infrastructure.jpa.querydsl.StudyQueryRepository;
import kr.spot.ports.GetPreferredRegionPort;
import kr.spot.presentation.query.dto.response.GetStudyOverviewResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetMyStudyInfoServiceTest {

    @Mock
    private GetPreferredRegionPort getPreferredRegionPort;

    @Mock
    private StudyQueryRepository studyQueryRepository;

    private GetMyStudyInfoService getMyStudyInfoService;

    @BeforeEach
    void setUp() {
        getMyStudyInfoService = new GetMyStudyInfoService(getPreferredRegionPort, studyQueryRepository);
    }

    private List<Study> createMockStudies(int count) {
        return LongStream.range(1, count + 1)
                .mapToObj(i -> Study.of(
                        i,
                        StudyFixture.LEADER_ID,
                        StudyFixture.NAME + " " + i,
                        StudyFixture.MAX_MEMBERS,
                        Fee.of(StudyFixture.HAS_FEE, StudyFixture.FEE_AMOUNT),
                        StudyFixture.IMAGE_URL,
                        StudyFixture.DESCRIPTION
                ))
                .collect(Collectors.toList());
    }

    @Test
    @DisplayName("요청한 페이지 사이즈보다 스터디가 많을 경우, hasNext가 true이고 다음 커서 ID를 반환한다.")
    void givenMoreStudiesThanPageSize_whenGetMyStudyOverview_thenReturnsHasNextTrueAndNextCursor() {
        // given
        Long viewerId = 1L;
        StudyMemberStatus status = StudyMemberStatus.OWNER;
        Long cursor = null;
        int size = 10;
        int pageSize = Math.min(size, GetMyStudyInfoService.MAX_PAGE_SIZE);

        List<Study> mockStudies = createMockStudies(pageSize + 1);
        when(studyQueryRepository.findMyStudies(viewerId, status, cursor, pageSize + 1))
                .thenReturn(mockStudies);

        // when
        GetStudyOverviewResponse response = getMyStudyInfoService.getMyStudyOverview(viewerId, status, cursor, size);

        // then
        assertThat(response.hasNext()).isTrue();
        assertThat(response.content()).hasSize(pageSize);
        assertThat(response.nextCursor()).isEqualTo(mockStudies.get(pageSize - 1).getId());
    }

    @Test
    @DisplayName("요청한 페이지 사이즈보다 스터디가 적을 경우, hasNext가 false이고 다음 커서 ID는 null이다.")
    void givenLessStudiesThanPageSize_whenGetMyStudyOverview_thenReturnsHasNextFalseAndNullCursor() {
        // given
        Long viewerId = 1L;
        StudyMemberStatus status = StudyMemberStatus.OWNER;
        Long cursor = null;
        int size = 10;
        int pageSize = Math.min(size, GetMyStudyInfoService.MAX_PAGE_SIZE);
        int studyCount = 5;

        List<Study> mockStudies = createMockStudies(studyCount);
        when(studyQueryRepository.findMyStudies(viewerId, status, cursor, pageSize + 1))
                .thenReturn(mockStudies);

        // when
        GetStudyOverviewResponse response = getMyStudyInfoService.getMyStudyOverview(viewerId, status, cursor, size);

        // then
        assertThat(response.hasNext()).isFalse();
        assertThat(response.content()).hasSize(studyCount);
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("스터디 개수가 페이지 사이즈와 정확히 일치할 경우, hasNext가 false이고 다음 커서 ID는 null이다.")
    void givenStudiesEqualToPageSize_whenGetMyStudyOverview_thenReturnsHasNextFalseAndNullCursor() {
        // given
        Long viewerId = 1L;
        StudyMemberStatus status = StudyMemberStatus.OWNER;
        Long cursor = null;
        int size = 10;
        int pageSize = Math.min(size, GetMyStudyInfoService.MAX_PAGE_SIZE);

        List<Study> mockStudies = createMockStudies(pageSize);
        when(studyQueryRepository.findMyStudies(viewerId, status, cursor, pageSize + 1))
                .thenReturn(mockStudies);

        // when
        GetStudyOverviewResponse response = getMyStudyInfoService.getMyStudyOverview(viewerId, status, cursor, size);

        // then
        assertThat(response.hasNext()).isFalse();
        assertThat(response.content()).hasSize(pageSize);
        assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("요청 사이즈가 MAX_PAGE_SIZE를 초과하면, 페이지 사이즈는 MAX_PAGE_SIZE로 제한된다.")
    void givenSizeGreaterThanMax_whenGetMyStudyOverview_thenPageSizeIsCapped() {
        // given
        Long viewerId = 1L;
        StudyMemberStatus status = StudyMemberStatus.OWNER;
        Long cursor = null;
        int size = GetMyStudyInfoService.MAX_PAGE_SIZE + 10;
        int pageSize = GetMyStudyInfoService.MAX_PAGE_SIZE;

        List<Study> mockStudies = createMockStudies(pageSize + 1);
        when(studyQueryRepository.findMyStudies(viewerId, status, cursor, pageSize + 1))
                .thenReturn(mockStudies);

        // when
        GetStudyOverviewResponse response = getMyStudyInfoService.getMyStudyOverview(viewerId, status, cursor, size);

        // then
        assertThat(response.hasNext()).isTrue();
        assertThat(response.content()).hasSize(pageSize);
        assertThat(response.nextCursor()).isEqualTo(mockStudies.get(pageSize - 1).getId());
    }
}
