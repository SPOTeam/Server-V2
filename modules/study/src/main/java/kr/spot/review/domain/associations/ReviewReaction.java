package kr.spot.review.domain.associations;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.spot.domain.BaseEntity;
import kr.spot.review.domain.enums.Reaction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE review_reaction SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewReaction extends BaseEntity {

  @Id
  private Long id;

  private Long reviewId;

  private Long memberId;

  private Reaction reaction;

  public static ReviewReaction of(Long id, Long reviewId, Long memberId, Reaction reaction) {
    return new ReviewReaction(id, reviewId, memberId, reaction);
  }
}
