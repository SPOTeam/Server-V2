package kr.spot.study.infrastructure.jpa.querydsl;

import static com.querydsl.jpa.JPAExpressions.selectOne;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.spot.study.domain.QStudy;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.associations.QStudyCategory;
import kr.spot.study.domain.associations.QStudyLike;
import kr.spot.study.domain.associations.QStudyMember;
import kr.spot.study.domain.associations.QStudyRegion;
import kr.spot.study.domain.enums.Category;
import kr.spot.study.domain.enums.FeeCategory;
import kr.spot.study.domain.enums.RecruitingStatus;
import kr.spot.study.domain.enums.SortBy;
import kr.spot.study.domain.enums.StudyMemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyQueryRepository {

  private final JPAQueryFactory query;

  public List<Study> findMyStudies(
      Long viewerId,
      List<StudyMemberStatus> statuses,
      Long cursor,
      int limit) {
    QStudy study = QStudy.study;
    QStudyMember studyMember = QStudyMember.studyMember;

    return query
        .select(study)
        .from(studyMember)
        .join(study).on(study.id.eq(studyMember.studyId))
        .where(
            studyMember.memberId.eq(viewerId),
            studyMember.studyMemberStatus.in(statuses),
            ltCursor(cursor, study)
        )
        .orderBy(study.id.desc())
        .limit(limit)
        .fetch();
  }

  public long countMyStudies(
      Long viewerId,
      List<StudyMemberStatus> statuses
  ) {
    QStudy study = QStudy.study;
    QStudyMember studyMember = QStudyMember.studyMember;

    return query
        .select(study.id.countDistinct())
        .from(studyMember)
        .join(study).on(study.id.eq(studyMember.studyId))
        .where(
            studyMember.memberId.eq(viewerId),
            studyMember.studyMemberStatus.in(statuses)
        )
        .fetchOne();
  }

  public List<Study> findMyPreferredRegionStudies(
      RecruitingStatus recruitingStatus,
      FeeCategory feeCategory,
      List<Category> categories,
      Boolean isOnline,
      SortBy sortBy,
      Long cursor,
      int limit,
      List<String> regionCodes
  ) {
    QStudy study = QStudy.study;
    QStudyRegion studyRegion = QStudyRegion.studyRegion;

    return query
        .select(study)
        .from(study)
        .where(
            existsRegion(study, studyRegion, regionCodes),
            existsCategories(study, categories),
            eqRecruitingStatus(recruitingStatus, study),
            eqFeeCategory(feeCategory, study),
            eqIsOnline(isOnline, study),
            ltCursor(cursor, study)
        )
        .orderBy(
            orderBy(sortBy, study),
            study.id.desc()
        )
        .limit(limit)
        .fetch();
  }

  public long countMyPreferredRegionStudies(
      RecruitingStatus recruitingStatus,
      FeeCategory feeCategory,
      List<Category> categories,
      Boolean isOnline,
      List<String> regionCodes
  ) {
    QStudy study = QStudy.study;
    QStudyRegion studyRegion = QStudyRegion.studyRegion;

    return query
        .select(study.id.count())
        .from(study)
        .where(
            existsRegion(study, studyRegion, regionCodes),
            existsCategories(study, categories),
            eqRecruitingStatus(recruitingStatus, study),
            eqFeeCategory(feeCategory, study),
            eqIsOnline(isOnline, study)
        )
        .fetchOne();
  }

  public List<Study> findMyPreferredCategoryStudies(
      RecruitingStatus recruitingStatus,
      FeeCategory feeCategory,
      Boolean isOnline,
      SortBy sortBy,
      Long cursor,
      int limit,
      List<Category> categories
  ) {
    QStudy study = QStudy.study;

    return query
        .select(study)
        .from(study)
        .where(
            existsCategories(study, categories),
            eqRecruitingStatus(recruitingStatus, study),
            eqFeeCategory(feeCategory, study),
            eqIsOnline(isOnline, study),
            ltCursor(cursor, study)
        )
        .orderBy(
            orderBy(sortBy, study),
            study.id.desc()
        )
        .limit(limit)
        .fetch();
  }

  public long countMyPreferredCategoryStudies(
      RecruitingStatus recruitingStatus,
      FeeCategory feeCategory,
      Boolean isOnline,
      List<Category> categories
  ) {
    QStudy study = QStudy.study;

    return query
        .select(study.id.count())
        .from(study)
        .where(
            existsCategories(study, categories),
            eqRecruitingStatus(recruitingStatus, study),
            eqFeeCategory(feeCategory, study),
            eqIsOnline(isOnline, study)
        )
        .fetchOne();
  }

  public List<Study> findRecruitingStudies(
      FeeCategory feeCategory,
      List<Category> categories,
      Boolean isOnline,
      SortBy sortBy,
      Long cursor,
      int limit
  ) {
    QStudy study = QStudy.study;

    return query
        .select(study)
        .from(study)
        .where(
            study.recruitingStatus.eq(RecruitingStatus.RECRUITING),
            existsCategories(study, categories),
            eqFeeCategory(feeCategory, study),
            eqIsOnline(isOnline, study),
            ltCursor(cursor, study)
        )
        .orderBy(
            orderBy(sortBy, study),
            study.id.desc()
        )
        .limit(limit)
        .fetch();
  }

  public long countRecruitingStudies(
      FeeCategory feeCategory,
      List<Category> categories,
      Boolean isOnline
  ) {
    QStudy study = QStudy.study;

    return query
        .select(study.id.count())
        .from(study)
        .where(
            study.recruitingStatus.eq(RecruitingStatus.RECRUITING),
            existsCategories(study, categories),
            eqFeeCategory(feeCategory, study),
            eqIsOnline(isOnline, study)
        )
        .fetchOne();
  }

  public List<Study> findStudiesByCategory(
      RecruitingStatus recruitingStatus,
      FeeCategory feeCategory,
      Category category,
      Boolean isOnline,
      SortBy sortBy,
      Long cursor,
      int limit
  ) {
    QStudy study = QStudy.study;

    return query
        .select(study)
        .from(study)
        .where(
            existsCategory(study, category),
            eqRecruitingStatus(recruitingStatus, study),
            eqFeeCategory(feeCategory, study),
            eqIsOnline(isOnline, study),
            ltCursor(cursor, study)
        )
        .orderBy(
            orderBy(sortBy, study),
            study.id.desc()
        )
        .limit(limit)
        .fetch();
  }

  public long countStudiesByCategory(
      RecruitingStatus recruitingStatus,
      FeeCategory feeCategory,
      Category category,
      Boolean isOnline
  ) {
    QStudy study = QStudy.study;

    return query
        .select(study.id.count())
        .from(study)
        .where(
            existsCategory(study, category),
            eqRecruitingStatus(recruitingStatus, study),
            eqFeeCategory(feeCategory, study),
            eqIsOnline(isOnline, study)
        )
        .fetchOne();
  }

  private BooleanExpression eqRecruitingStatus(RecruitingStatus recruitingStatus, QStudy study) {
    return recruitingStatus == null ? null : study.recruitingStatus.eq(recruitingStatus);
  }

  private BooleanExpression eqFeeCategory(FeeCategory feeCategory, QStudy study) {
    return feeCategory == null ? null : study.fee.feeCategory.eq(feeCategory);
  }

  private BooleanExpression eqIsOnline(Boolean isOnline, QStudy study) {
    return isOnline == null ? null : study.isOnline.eq(isOnline);
  }

  private BooleanExpression ltCursor(Long cursor, QStudy study) {
    return cursor == null ? null : study.id.lt(cursor);
  }

  private BooleanExpression existsCategories(QStudy study, List<Category> categories) {
    if (categories == null || categories.isEmpty()) {
      return null;
    }
    QStudyCategory studyCategory = QStudyCategory.studyCategory;
    return selectOne()
        .from(studyCategory)
        .where(
            studyCategory.studyId.eq(study.id),
            studyCategory.category.in(categories)
        )
        .exists();
  }

  private BooleanExpression existsCategory(QStudy study, Category category) {
    if (category == null) {
      return null;
    }
    QStudyCategory studyCategory = QStudyCategory.studyCategory;
    return selectOne()
        .from(studyCategory)
        .where(
            studyCategory.studyId.eq(study.id),
            studyCategory.category.eq(category)
        )
        .exists();
  }

  private BooleanExpression existsRegion(QStudy study, QStudyRegion studyRegion, List<String> regionCodes) {
    if (regionCodes == null || regionCodes.isEmpty()) {
      return null;
    }
    return selectOne()
        .from(studyRegion)
        .where(
            studyRegion.studyId.eq(study.id),
            studyRegion.regionCode.in(regionCodes)
        )
        .exists();
  }

  private OrderSpecifier<?> orderBy(SortBy sortBy, QStudy study) {
    if (sortBy == null) {
      return study.id.desc();
    }

    return switch (sortBy) {
      case RECENT -> study.id.desc();
      case LIKES -> study.likeCount.desc();
      case HITS -> study.viewCount.desc();
    };
  }

  public List<Study> findLikedStudies(long memberId, Long cursor, int limit) {
    QStudy study = QStudy.study;
    QStudyLike studyLike = QStudyLike.studyLike;

    return query
        .select(study)
        .from(studyLike)
        .join(study).on(study.id.eq(studyLike.studyId))
        .where(
            studyLike.memberId.eq(memberId),
            ltCursor(cursor, study)
        )
        .orderBy(study.id.desc())
        .limit(limit)
        .fetch();
  }

  public long countLikedStudies(long memberId) {
    QStudy study = QStudy.study;
    QStudyLike studyLike = QStudyLike.studyLike;

    return query
        .select(study.id.countDistinct())
        .from(studyLike)
        .join(study).on(study.id.eq(studyLike.studyId))
        .where(studyLike.memberId.eq(memberId))
        .fetchOne();
  }

  public List<Study> findRecruitingStudiesByCategories(List<Category> categories, int limit) {
    QStudy study = QStudy.study;

    return query
        .select(study)
        .from(study)
        .where(
            existsCategories(study, categories),
            study.recruitingStatus.eq(RecruitingStatus.RECRUITING)
        )
        .orderBy(study.id.desc())
        .limit(limit)
        .fetch();
  }

  public List<Study> findPopularRecruitingStudies(List<Long> excludeIds, int limit) {
    QStudy study = QStudy.study;

    return query
        .select(study)
        .from(study)
        .where(
            study.recruitingStatus.eq(RecruitingStatus.RECRUITING),
            notInIds(excludeIds, study)
        )
        .orderBy(study.likeCount.desc(), study.id.desc())
        .limit(limit)
        .fetch();
  }

  private BooleanExpression notInIds(List<Long> excludeIds, QStudy study) {
    if (excludeIds == null || excludeIds.isEmpty()) {
      return null;
    }
    return study.id.notIn(excludeIds);
  }
}
