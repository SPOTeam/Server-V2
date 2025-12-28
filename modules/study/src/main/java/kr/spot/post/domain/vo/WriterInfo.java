package kr.spot.post.domain.vo;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
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
public class WriterInfo {

  private Long writerId;

  private String writerName;

  private String WriterProfileImageUrl;

  public static WriterInfo of(Long writerId, String writerName, String writerProfileImageUrl) {
    return new WriterInfo(writerId, writerName, writerProfileImageUrl);
  }

  public void validateIsOwnMember(Long currentUserId) {
    if (!Objects.equals(writerId, currentUserId)) {
      throw new GeneralException(ErrorStatus._ONLY_AUTHOR_CAN_MODIFY);
    }
  }

  public boolean isSameWriter(Long memberId) {
    return Objects.equals(this.writerId, memberId);
  }
}
