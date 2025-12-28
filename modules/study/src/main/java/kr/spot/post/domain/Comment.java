package kr.spot.post.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.spot.domain.BaseEntity;
import kr.spot.post.domain.vo.WriterInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity(name = "StudyPostComment")
@SQLDelete(sql = "UPDATE study_post_comment SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Comment extends BaseEntity {

  @Id
  private Long id;

  private Long postId;

  private WriterInfo writerInfo;

  private String content;

  public static Comment of(Long id, Long postId, WriterInfo writerInfo, String content) {
    return new Comment(id, postId, writerInfo, content);
  }

  public void update(Long currentMemberId, String content) {
    writerInfo.validateIsOwnMember(currentMemberId);
    this.content = content;
  }

  public void delete(Long currentMemberId) {
    writerInfo.validateIsOwnMember(currentMemberId);
    super.delete();
  }
}
