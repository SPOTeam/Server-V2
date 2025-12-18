package kr.spot.domain.vo;

import static io.micrometer.common.util.StringUtils.isBlank;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
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
public class Email {

  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

  @Column(nullable = false, name = "email")
  private String value;

  public static Email of(String value) {
    if (isBlank(value) || !EMAIL_PATTERN.matcher(value).matches()) {
      throw new GeneralException(ErrorStatus._INVALID_EMAIL_FORMAT);
    }
    return new Email(value);
  }
}
