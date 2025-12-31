package kr.spot.study.application.command;

import kr.spot.IdGenerator;
import kr.spot.study.infrastructure.jpa.associations.StudyLikeRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyLikeService {

  private final IdGenerator idGenerator;
  private final StudyLikeRepository studyLikeRepository;
  private final StudyStatsRepository studyStatsRepository;

  public void likeStudy(long studyId, long memberId) {
    int inserted = studyLikeRepository.saveStudyLike(idGenerator.nextId(), studyId, memberId);
    increaseLikeCount(studyId, inserted);
  }

  private void increaseLikeCount(long studyId, int inserted) {
    if (inserted == 1) {
      studyStatsRepository.increaseLike(studyId);
    }
  }

  public void unlikeStudy(long studyId, long memberId) {
    int deleted = studyLikeRepository.hardDelete(studyId, memberId);
    if (deleted > 0) {
      studyStatsRepository.decreaseLike(studyId);
    }
  }
}
