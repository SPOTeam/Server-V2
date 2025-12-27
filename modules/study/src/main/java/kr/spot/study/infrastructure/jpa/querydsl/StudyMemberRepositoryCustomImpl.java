package kr.spot.study.infrastructure.jpa.querydsl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.spot.study.domain.QStudy;
import kr.spot.study.domain.associations.QStudyMember;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.infrastructure.jpa.StudyMemberRepositoryCustom;
import kr.spot.study.infrastructure.jpa.querydsl.dto.StudyApplicationInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StudyMemberRepositoryCustomImpl implements StudyMemberRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<StudyApplicationInfo> findMyAppliedStudiesWithStudyInfo(
      Long memberId,
      StudyMemberStatus status
  ) {
    QStudyMember studyMember = QStudyMember.studyMember;
    QStudy study = QStudy.study;

    return queryFactory
        .select(Projections.constructor(
            StudyApplicationInfo.class,
            studyMember.id,
            studyMember.studyId,
            study.name,
            study.imageUrl
        ))
        .from(studyMember)
        .join(study).on(studyMember.studyId.eq(study.id))
        .where(
            studyMember.memberId.eq(memberId),
            studyMember.studyMemberStatus.eq(status)
        )
        .orderBy(studyMember.createdAt.desc())
        .fetch();
  }

  @Override
  public List<StudyMember> findApplicationsByStudyIdAndStatus(Long studyId,
      StudyMemberStatus status) {

    QStudyMember studyMember = QStudyMember.studyMember;

    return queryFactory
        .selectFrom(studyMember)
        .where(
            studyMember.studyId.eq(studyId),
            studyMember.studyMemberStatus.eq(status)
        )
        .orderBy(studyMember.createdAt.asc())
        .fetch();
  }
}
