package kr.spot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;


@Getter
@Entity
@SQLDelete(sql = "UPDATE region SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Region extends BaseEntity {

  @Id
  private String code;

  private String province;

  private String district;

  private String neighborhood;

  public static Region of(String code, String province, String district, String neighborhood) {
    return new Region(code, province, district, neighborhood);
  }
}
