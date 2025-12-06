package kr.spot.infrastructure.jpa.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.spot.domain.QStudy;
import kr.spot.domain.Study;
import kr.spot.domain.associations.QStudyCategory;
import kr.spot.domain.associations.QStudyStats;
import kr.spot.domain.enums.Category;
import kr.spot.domain.enums.FeeCategory;
import kr.spot.domain.enums.RecruitingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyQueryTestRepository {

  private final JPAQueryFactory query;

  public List<Study> findStudyWithRange(
      boolean hasFee,
      int fee,
      RecruitingStatus recruitingStatus,
      List<Category> categories,
      Long cursor,
      int limit
  ) {
    QStudy study = QStudy.study;
    QStudyCategory studyCategory = QStudyCategory.studyCategory;
    QStudyStats studyStats = QStudyStats.studyStats;

    return query
        .select(study).distinct()
        .from(study)
        .leftJoin(studyCategory).on(studyCategory.studyId.eq(study.id))
        .leftJoin(studyStats).on(studyStats.studyId.eq(study.id))
        .where(
            eqRecruitingStatus(recruitingStatus, study),
            inCategories(categories, studyCategory),
            ltCursor(cursor, study),
            hasFee ? study.fee.amount.lt(fee) : null
        )
        .orderBy(
            study.id.desc()
        )
        .limit(limit)
        .fetch();
  }

  public List<Study> findStudyWithCategory(
      FeeCategory feeCategory,
      RecruitingStatus recruitingStatus,
      List<Category> categories,
      Long cursor,
      int limit
  ) {
    QStudy study = QStudy.study;
    QStudyCategory studyCategory = QStudyCategory.studyCategory;
    QStudyStats studyStats = QStudyStats.studyStats;

    return query
        .select(study).distinct()
        .from(study)
        .leftJoin(studyCategory).on(studyCategory.studyId.eq(study.id))
        .leftJoin(studyStats).on(studyStats.studyId.eq(study.id))
        .where(
            eqRecruitingStatus(recruitingStatus, study),
            inCategories(categories, studyCategory),
            ltCursor(cursor, study),
            eqFeeCategory(feeCategory, study)
        )
        .orderBy(
            study.id.desc()
        )
        .limit(limit)
        .fetch();
  }

  private BooleanExpression eqFeeCategory(FeeCategory feeCategory, QStudy study) {
    return feeCategory == null ? null : study.fee.feeCategory.eq(feeCategory);
  }

  private BooleanExpression eqRecruitingStatus(RecruitingStatus recruitingStatus, QStudy study) {
    return recruitingStatus == null ? null : study.recruitingStatus.eq(recruitingStatus);
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
}
