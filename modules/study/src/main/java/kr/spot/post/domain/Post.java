package kr.spot.post.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.BaseEntity;
import kr.spot.exception.GeneralException;
import kr.spot.post.domain.vo.WriterInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity(name = "StudyPost")
@SQLDelete(sql = "UPDATE study_post SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Post extends BaseEntity {

  @Id
  private Long id;

  private Long studyId;

  @Embedded
  private WriterInfo writerInfo;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String content;

  private LocalDateTime pinnedAt;

  private boolean isPrivate;

  public static Post of(Long id, Long studyId, WriterInfo writerInfo, String title, String content,
      boolean isPrivate) {
    return new Post(id, studyId, writerInfo, title, content, null, isPrivate);
  }

  public void update(String title, String content, boolean isPrivate, long memberId, long studyId) {
    validateStudyId(studyId);
    writerInfo.validateIsOwnMember(memberId);
    this.title = title;
    this.content = content;
    this.isPrivate = isPrivate;
  }

  public void delete(long memberId, long studyId) {
    validateStudyId(studyId);
    writerInfo.validateIsOwnMember(memberId);
    super.delete();
  }

  public void pin(long studyId) {
    validateStudyId(studyId);
    this.pinnedAt = LocalDateTime.now();
  }

  public void unpin(long studyId) {
    validateStudyId(studyId);
    this.pinnedAt = null;
  }

  public boolean isPinned() {
    return pinnedAt != null;
  }

  public boolean isOwnedBy(long memberId) {
    return writerInfo.isSameWriter(memberId);
  }

  public void validateBelongsToStudy(long studyId) {
    validateStudyId(studyId);
  }

  public void validatePublicAccess() {
    if (this.isPrivate) {
      throw new GeneralException(ErrorStatus._PRIVATE_POST_ACCESS_DENIED);
    }
  }

  private void validateStudyId(long studyId) {
    if (this.studyId != studyId) {
      throw new GeneralException(ErrorStatus._INVALID_STUDY_ACCESS);
    }
  }
}
