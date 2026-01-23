package kr.spot.study.application.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import kr.spot.ports.GetPreferredCategoryPort;
import kr.spot.ports.GetPreferredRegionPort;
import kr.spot.study.application.mapper.StudyDTOMapper;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyStudyInfoService {

  public static final int MAX_PAGE_SIZE = 50;

  private final GetPreferredRegionPort getPreferredRegionPort;
  private final GetPreferredCategoryPort getPreferredCategoryPort;
  private final StudyQueryRepository studyQueryRepository;
  private final StudyLikeRepository studyLikeRepository;
  private final StudyMemberRepository studyMemberRepository;

  public GetStudyOverviewResponse getMyStudyOverview(
      long viewerId,
      List<StudyMemberStatus> statuses,
      Long cursor,
      int size
  ) {
    final int pageSize = Math.min(size, MAX_PAGE_SIZE);
    List<Study> rows = studyQueryRepository.findMyStudies(
        viewerId,
        statuses,
        cursor,
        pageSize + 1
    );

    long totalElements = studyQueryRepository.countMyStudies(
        viewerId,
        statuses
    );
    return toCursorPage(rows, viewerId, pageSize, totalElements);
  }

  public GetStudyOverviewResponse getMyPreferredRegionStudies(
      long viewerId,
      RecruitingStatus recruitingStatus,
      FeeCategory feeCategory,
      List<Category> categories,
      Boolean isOnline,
      SortBy sortBy,
      Long cursor,
      int size,
      List<String> regionCodes
  ) {
    final int pageSize = Math.min(size, MAX_PAGE_SIZE);
    List<String> preferredRegionCodes = getPreferredRegionPort.get(viewerId);
    List<String> filteredRegionCodes = filterPreferredRegionCodes(regionCodes,
        preferredRegionCodes);

    List<Study> rows = studyQueryRepository.findMyPreferredRegionStudies(
        recruitingStatus,
        feeCategory,
        categories,
        isOnline,
        sortBy,
        cursor,
        pageSize + 1,
        filteredRegionCodes
    );

    long totalElements = studyQueryRepository.countMyPreferredRegionStudies(
        recruitingStatus,
        feeCategory,
        categories,
        isOnline,
        filteredRegionCodes
    );
    return toCursorPage(rows, viewerId, pageSize, totalElements);
  }

  public GetStudyOverviewResponse getMyPreferredCategoryStudies(
      long viewerId,
      Category category,
      RecruitingStatus recruitingStatus,
      FeeCategory feeCategory,
      Boolean isOnline,
      SortBy sortBy,
      Long cursor,
      int size
  ) {
    final int pageSize = Math.min(size, MAX_PAGE_SIZE);
    List<Category> preferredCategories = getCategories(viewerId, category);

    List<Study> rows = studyQueryRepository.findMyPreferredCategoryStudies(
        recruitingStatus,
        feeCategory,
        isOnline,
        sortBy,
        cursor,
        pageSize + 1,
        preferredCategories
    );

    long totalElements = studyQueryRepository.countMyPreferredCategoryStudies(
        recruitingStatus,
        feeCategory,
        isOnline,
        preferredCategories
    );
    return toCursorPage(rows, viewerId, pageSize, totalElements);
  }

  public GetStudyOverviewResponse getRecruitingStudies(
      long viewerId,
      FeeCategory feeCategory,
      List<Category> categories,
      Boolean isOnline,
      SortBy sortBy,
      Long cursor,
      int size
  ) {
    final int pageSize = Math.min(size, MAX_PAGE_SIZE);

    List<Study> rows = studyQueryRepository.findRecruitingStudies(
        feeCategory,
        categories,
        isOnline,
        sortBy,
        cursor,
        pageSize + 1
    );

    long totalElements = studyQueryRepository.countRecruitingStudies(
        feeCategory,
        categories,
        isOnline
    );
    return toCursorPage(rows, viewerId, pageSize, totalElements);
  }

  public GetStudyOverviewResponse getStudiesByCategory(
      long viewerId,
      RecruitingStatus recruitingStatus,
      FeeCategory feeCategory,
      Category category,
      Boolean isOnline,
      SortBy sortBy,
      Long cursor,
      int size
  ) {
    final int pageSize = Math.min(size, MAX_PAGE_SIZE);

    List<Study> rows = studyQueryRepository.findStudiesByCategory(
        recruitingStatus,
        feeCategory,
        category,
        isOnline,
        sortBy,
        cursor,
        pageSize + 1
    );

    long totalElements = studyQueryRepository.countStudiesByCategory(
        recruitingStatus,
        feeCategory,
        category,
        isOnline
    );
    return toCursorPage(rows, viewerId, pageSize, totalElements);
  }

  public GetStudyOverviewResponse getLikedStudies(long viewerId, Long cursor, int size) {
    final int pageSize = Math.min(size, MAX_PAGE_SIZE);
    List<Study> rows = studyQueryRepository.findLikedStudies(viewerId, cursor, pageSize + 1);
    long totalElements = studyQueryRepository.countLikedStudies(viewerId);
    return toCursorPage(rows, viewerId, pageSize, totalElements);
  }

  public GetStudyOverviewResponse getRecommendedStudies(long viewerId) {
    final int recommendCount = 3;

    List<Category> preferredCategories = getPreferredCategories(viewerId);
    List<Study> result = pickRandomStudiesFromPreferred(preferredCategories, recommendCount);
    fillWithPopularStudies(result, recommendCount);

    Set<Long> likedStudyIds = studyLikeRepository.findStudyIdsByMemberId(viewerId);
    Set<Long> ownedStudyIds = studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(
        viewerId, StudyMemberStatus.OWNER);
    Set<Long> aloneStudyIds = studyMemberRepository.findAloneOwnerStudyIds(viewerId);
    return StudyDTOMapper.toDTO(result, likedStudyIds, ownedStudyIds, aloneStudyIds, false, null,
        (long) result.size());
  }

  private List<Category> getPreferredCategories(long memberId) {
    return getPreferredCategoryPort.get(memberId)
        .stream()
        .map(Category::fromString)
        .toList();
  }

  private List<Study> pickRandomStudiesFromPreferred(List<Category> categories, int count) {
    if (categories.isEmpty()) {
      return new ArrayList<>();
    }

    final int fetchLimit = 20;
    List<Study> candidates = studyQueryRepository.findRecruitingStudiesByCategories(
        categories, fetchLimit);

    List<Study> shuffled = new ArrayList<>(candidates);
    Collections.shuffle(shuffled);

    return new ArrayList<>(shuffled.stream().limit(count).toList());
  }

  private void fillWithPopularStudies(List<Study> result, int targetCount) {
    if (result.size() >= targetCount) {
      return;
    }

    List<Long> excludeIds = result.stream().map(Study::getId).toList();
    int remaining = targetCount - result.size();

    List<Study> popularStudies = studyQueryRepository.findPopularRecruitingStudies(
        excludeIds, remaining);
    result.addAll(popularStudies);
  }

  private GetStudyOverviewResponse toCursorPage(List<Study> rows, long viewerId, int pageSize,
      long totalElements) {
    boolean hasNext = rows.size() > pageSize;
    List<Study> pageContent = hasNext ? rows.subList(0, pageSize) : rows;
    Long nextCursor = hasNext ? pageContent.getLast().getId() : null;
    Set<Long> likedStudyIds = studyLikeRepository.findStudyIdsByMemberId(viewerId);
    Set<Long> ownedStudyIds = studyMemberRepository.findStudyIdsByMemberIdAndStudyMemberStatus(
        viewerId, StudyMemberStatus.OWNER);
    Set<Long> aloneStudyIds = studyMemberRepository.findAloneOwnerStudyIds(viewerId);
    return StudyDTOMapper.toDTO(pageContent, likedStudyIds, ownedStudyIds, aloneStudyIds, hasNext,
        nextCursor, totalElements);
  }

  private List<String> filterPreferredRegionCodes(List<String> regionCodes,
      List<String> preferredRegionCodes) {
    if (regionCodes == null || regionCodes.isEmpty()) {
      return preferredRegionCodes;
    }
    return preferredRegionCodes.stream()
        .filter(regionCodes::contains)
        .toList();
  }

  private List<Category> getCategories(Long viewerId, Category category) {
    List<Category> preferredRegionCodes;
    if (category == null) {
      preferredRegionCodes = getPreferredCategoryPort.get(viewerId)
          .stream()
          .map(Category::fromString)
          .toList();
    } else {
      preferredRegionCodes = List.of(category);
    }
    return preferredRegionCodes;
  }
}
