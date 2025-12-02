package kr.spot.domain.association;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.spot.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE preferred_category SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PreferredCategory extends BaseEntity {

  @Id
  private Long id;

  private Long memberId;

  private String category;

  public static PreferredCategory of(Long id, Long memberId, String category) {
    return new PreferredCategory(id, memberId, category);
  }
}
