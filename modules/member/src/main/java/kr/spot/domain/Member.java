package kr.spot.domain;


import static io.micrometer.common.util.StringUtils.isBlank;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.enums.LoginType;
import kr.spot.domain.vo.Email;
import kr.spot.exception.GeneralException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE member SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member extends BaseEntity {

  @Id
  private Long id;

  @Embedded
  @AttributeOverride(name = "value",
      column = @Column(name = "email", nullable = false, unique = true))
  private Email email;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private LoginType loginType;

  @Column
  private String profileImageUrl;

  /* ------------------------------- Method  ------------------------------- */

  public static Member of(Long id, Email email, String name, LoginType loginType,
      String profileImageUrl) {
    validateEmail(email);
    validateName(name);
    return new Member(id, email, name, loginType, profileImageUrl);
  }

  public void reactivate(String name, String profileImageUrl) {
    validateName(name);
    this.name = name;
    this.profileImageUrl = profileImageUrl;
    restore();
  }

  private static void validateName(String name) {
    if (isBlank(name)) {
      throw new GeneralException(ErrorStatus._NAME_CAN_NOT_NULL_OR_EMPTY);
    }
  }

  private static void validateEmail(Email email) {
    if (email == null) {
      throw new GeneralException(ErrorStatus._EMAIL_CAN_NOT_NULL_OR_EMPTY);
    }
  }
}
