package kr.spot.study.infrastructure.jpa.querydsl;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.spot.study.domain.QStudy;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.associations.QStudyCategory;
import kr.spot.study.domain.associations.QStudyMember;
import kr.spot.study.domain.associations.QStudyRegion;
import kr.spot.study.domain.associations.QStudyStats;
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
      StudyMemberStatus status,
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
            studyMember.studyMemberStatus.eq(status),
            ltCursor(cursor, study)
        )
        .orderBy(study.id.desc())
        .limit(limit)
        .fetch();
  }

  public long countMyStudies(
      Long viewerId,
      StudyMemberStatus status
  ) {
    QStudy study = QStudy.study;
    QStudyMember studyMember = QStudyMember.studyMember;

    return query
        .select(study.id.countDistinct())
        .from(studyMember)
        .join(study).on(study.id.eq(studyMember.studyId))
        .where(
            studyMember.memberId.eq(viewerId),
            studyMember.studyMemberStatus.eq(status)
        )
        .fetchOne();
  }

  public List<Study> findMyPreferredRegionStudies(
      RecruitingStatus recruitingStatus,
      FeeCategory feeCategory,
      List<Category> categories,
      SortBy sortBy,
      Long cursor,
      int limit,
      List<String> regionCodes
  ) {
    QStudy study = QStudy.study;
    QStudyRegion studyRegion = QStudyRegion.studyRegion;
    QStudyCategory studyCategory = QStudyCategory.studyCategory;
    QStudyStats studyStats = QStudyStats.studyStats;

    return query
        .select(study)
        .from(study)
        .join(studyRegion).on(studyRegion.studyId.eq(study.id))
        .leftJoin(studyCategory).on(studyCategory.studyId.eq(study.id))
        .leftJoin(studyStats).on(studyStats.studyId.eq(study.id))
        .where(
            studyRegion.regionCode.in(regionCodes),
            eqRecruitingStatus(recruitingStatus, study),
            eqFeeCategory(feeCategory, study),
            inCategories(categories, studyCategory),
            ltCursor(cursor, study)
        )
        .groupBy(study.id)
        .orderBy(
            orderBy(sortBy, study, studyStats),
            study.id.desc()
        )
        .limit(limit)
        .fetch();
  }

  public long countMyPreferredRegionStudies(
      RecruitingStatus recruitingStatus,
      FeeCategory feeCategory,
      List<Category> categories,
      List<String> regionCodes
  ) {
    QStudy study = QStudy.study;
    QStudyRegion studyRegion = QStudyRegion.studyRegion;
    QStudyCategory studyCategory = QStudyCategory.studyCategory;

    return query
        .select(study.id.countDistinct())
        .from(study)
        .join(studyRegion).on(studyRegion.studyId.eq(study.id))
        .leftJoin(studyCategory).on(studyCategory.studyId.eq(study.id))
        .where(
            studyRegion.regionCode.in(regionCodes),
            eqRecruitingStatus(recruitingStatus, study),
            eqFeeCategory(feeCategory, study),
            inCategories(categories, studyCategory)
        )
        .fetchOne();
  }

  public List<Study> findMyPreferredCategoryStudies(
      RecruitingStatus recruitingStatus,
      FeeCategory feeCategory,
      SortBy sortBy,
      Long cursor,
      int limit,
      List<Category> categories
  ) {
    QStudy study = QStudy.study;
    QStudyCategory studyCategory = QStudyCategory.studyCategory;
    QStudyStats studyStats = QStudyStats.studyStats;

    return query
        .select(study)
        .from(study)
        .leftJoin(studyCategory).on(studyCategory.studyId.eq(study.id))
        .leftJoin(studyStats).on(studyStats.studyId.eq(study.id))
        .where(
            eqRecruitingStatus(recruitingStatus, study),
            eqFeeCategory(feeCategory, study),
            inCategories(categories, studyCategory),
            ltCursor(cursor, study)
        )
        .groupBy(study.id)
        .orderBy(
            orderBy(sortBy, study, studyStats),
            study.id.desc()
        )
        .limit(limit)
        .fetch();
  }

  public long countMyPreferredCategoryStudies(
      RecruitingStatus recruitingStatus,
      FeeCategory feeCategory,
      List<Category> categories
  ) {
    QStudy study = QStudy.study;
    QStudyCategory studyCategory = QStudyCategory.studyCategory;

    return query
        .select(study.id.countDistinct())
        .from(study)
        .leftJoin(studyCategory).on(studyCategory.studyId.eq(study.id))
        .where(
            eqRecruitingStatus(recruitingStatus, study),
            eqFeeCategory(feeCategory, study),
            inCategories(categories, studyCategory)
        )
        .fetchOne();
  }

  private BooleanExpression eqRecruitingStatus(RecruitingStatus recruitingStatus, QStudy study) {
    return recruitingStatus == null ? null : study.recruitingStatus.eq(recruitingStatus);
  }

  private BooleanExpression eqFeeCategory(FeeCategory feeCategory, QStudy study) {
    return feeCategory == null ? null : study.fee.feeCategory.eq(feeCategory);
  }

  private BooleanExpression inCategories(List<Category> categories, QStudyCategory studyCategory) {
    if (categories == null || categories.isEmpty()) {
      return null;
    }
    return studyCategory.category.in(categories);
  }

  private BooleanExpression ltCursor(Long cursor, QStudy study) {
    return cursor == null ? null : study.id.lt(cursor);
  }

  private OrderSpecifier<?> orderBy(SortBy sortBy, QStudy study, QStudyStats studyStats) {
    if (sortBy == null) {
      return study.id.desc();
    }

    return switch (sortBy) {
      case RECENT -> study.id.desc();
      case LIKES -> studyStats.likeCount.desc();
      case HITS -> studyStats.viewCount.desc();
    };
  }
}
