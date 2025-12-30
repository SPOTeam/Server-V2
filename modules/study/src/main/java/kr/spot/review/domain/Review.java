package kr.spot.review.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.spot.domain.BaseEntity;
import kr.spot.review.domain.vo.Content;
import kr.spot.review.domain.vo.WriterInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE review SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Review extends BaseEntity {

  @Id
  private Long id;

  @Embedded
  private WriterInfo writerInfo;

  @Embedded
  private Content content;

  public static Review of(Long id, WriterInfo writerInfo, Content content) {
    return new Review(id, writerInfo, content);
  }
}
