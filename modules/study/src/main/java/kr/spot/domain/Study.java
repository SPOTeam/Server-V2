package kr.spot.domain;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.enums.RecruitingStatus;
import kr.spot.domain.vo.Fee;
import kr.spot.exception.GeneralException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE study SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Study extends BaseEntity {

  public static final int CURRENT_MEMBERS = 1;
  @Id
  private Long id;

  private Long leaderId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Integer maxMembers;

  @Column(nullable = false)
  private Integer currentMembers = 1;

  @Embedded
  private Fee fee;

  @Column
  private String imageUrl;

  @Column(length = 1000)
  private String description;

  @Enumerated(EnumType.STRING)
  private RecruitingStatus recruitingStatus;

  public static Study of(Long id, Long leaderId, String name, Integer maxMembers, Fee fee,
      String imageUrl,
      String description) {
    validateStudyNameIsNotBlank(name);
    validateMaxMembers(maxMembers);
    return new Study(id, leaderId, name, maxMembers, CURRENT_MEMBERS, fee, imageUrl, description,
        RecruitingStatus.RECRUITING);
  }

  private static void validateStudyNameIsNotBlank(String name) {
    if (StringUtils.isBlank(name)) {
      throw new GeneralException(ErrorStatus._NAME_CAN_NOT_NULL_OR_EMPTY);
    }
  }

  private static void validateMaxMembers(Integer maxMembers) {
    if (maxMembers != null && maxMembers <= 0) {
      throw new GeneralException(ErrorStatus._MAX_MEMBERS_MUST_BE_POSITIVE);
    }
  }
}
