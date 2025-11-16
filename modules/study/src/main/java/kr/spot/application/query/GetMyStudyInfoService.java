package kr.spot.application.query;

import java.util.List;
import kr.spot.application.mapper.StudyDTOMapper;
import kr.spot.domain.Study;
import kr.spot.domain.enums.Category;
import kr.spot.domain.enums.FeeCategory;
import kr.spot.domain.enums.RecruitingStatus;
import kr.spot.domain.enums.SortBy;
import kr.spot.domain.enums.StudyMemberStatus;
import kr.spot.infrastructure.jpa.querydsl.StudyQueryRepository;
import kr.spot.ports.GetPreferredRegionPort;
import kr.spot.presentation.query.dto.response.GetStudyOverviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyStudyInfoService {
    public static final int MAX_PAGE_SIZE = 50;

    private final GetPreferredRegionPort getPreferredRegionPort;
    private final StudyQueryRepository studyQueryRepository;

    public GetStudyOverviewResponse getMyStudyOverview(
            Long viewerId,
            StudyMemberStatus status,
            Long cursor,
            int size
    ) {
        final int pageSize = Math.min(size, MAX_PAGE_SIZE);
        List<Study> rows = studyQueryRepository.findMyStudies(
                viewerId,
                status,
                cursor,
                pageSize + 1
        );

        long totalElements = studyQueryRepository.countMyStudies(
                viewerId,
                status
        );
        return toCursorPage(rows, pageSize, totalElements);
    }

    public GetStudyOverviewResponse getMyPreferredRegionStudies(
            Long viewerId,
            RecruitingStatus recruitingStatus,
            FeeCategory feeCategory,
            List<Category> categories,
            SortBy sortBy,
            Long cursor,
            Integer size,
            List<String> regionCodes
    ) {
        final int pageSize = Math.min(size, MAX_PAGE_SIZE);
        List<String> preferredRegionCodes = getPreferredRegionPort.get(viewerId);

        List<Study> rows = studyQueryRepository.findMyPreferredRegionStudies(
                recruitingStatus,
                feeCategory,
                categories,
                sortBy,
                cursor,
                pageSize + 1,
                filterPreferredRegionCodes(regionCodes, preferredRegionCodes)
        );

        long totalElements = studyQueryRepository.countMyPreferredRegionStudies(
                recruitingStatus,
                feeCategory,
                categories,
                filterPreferredRegionCodes(regionCodes, preferredRegionCodes)
        );
        return toCursorPage(rows, pageSize, totalElements);
    }

    private GetStudyOverviewResponse toCursorPage(List<Study> rows, int pageSize, Long totalElements) {
        boolean hasNext = rows.size() > pageSize;
        List<Study> pageContent = hasNext ? rows.subList(0, pageSize) : rows;
        Long nextCursor = hasNext ? pageContent.getLast().getId() : null;
        return StudyDTOMapper.toDTO(pageContent, hasNext, nextCursor, totalElements);
    }

    private List<String> filterPreferredRegionCodes(List<String> regionCodes, List<String> preferredRegionCodes) {
        if (regionCodes == null || regionCodes.isEmpty()) {
            return preferredRegionCodes;
        }
        return preferredRegionCodes.stream()
                .filter(regionCodes::contains)
                .toList();
    }
}
