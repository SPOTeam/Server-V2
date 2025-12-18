package kr.spot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.enums.PostType;
import kr.spot.domain.vo.WriterInfo;
import kr.spot.exception.GeneralException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE post SET status = 'INACTIVE' WHERE id = ?")
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

  @Enumerated(EnumType.STRING)
  private PostType postType;

  public static Post of(Long id, WriterInfo writerInfo, String title, String content,
      PostType postType) {
    return new Post(id, writerInfo, title, content, postType);
  }

  public void validateIsWriter(Long memberId) {
    if (!this.writerInfo.getWriterId().equals(memberId)) {
      throw new GeneralException(ErrorStatus._ONLY_AUTHOR_CAN_MODIFY);
    }
  }

  public void update(String title, String content, PostType postType, Long memberId) {
    validateIsWriter(memberId);
    this.title = title;
    this.content = content;
    this.postType = postType;
  }

  public void delete(Long memberId) {
    validateIsWriter(memberId);
    super.delete();
  }

  public boolean isWrittenBy(Long memberId) {
    return this.writerInfo.isSameWriter(memberId);
  }
}
