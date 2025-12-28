package kr.spot.post;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.spot.domain.BaseEntity;
import kr.spot.post.vo.WriterInfo;
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

  @Embedded
  private WriterInfo writerInfo;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String content;

  private boolean isPinned;

  private boolean isPrivate;

  public static Post of(Long id, WriterInfo writerInfo, String title, String content,
      boolean isPinned, boolean isPrivate) {
    return new Post(id, writerInfo, title, content, isPinned, isPrivate);
  }
}
