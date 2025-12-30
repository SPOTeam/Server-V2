package kr.spot.schedule.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberInfo {

  private Long memberId;

  private String memberName;

  private String memberProfileImageUrl;

  public static MemberInfo of(Long memberId, String memberName, String memberProfileImageUrl) {
    return new MemberInfo(memberId, memberName, memberProfileImageUrl);
  }
}