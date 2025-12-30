package kr.spot.review.domain.vo;

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
public class Content {

  // 오늘은 무엇을 했나요?
  private String activity;

  // 오늘 새롭게 배운 점은 무엇인가요?
  private String learned;

  // 고생한 나에게 한마디
  private String encouragement;

  private String imageUrl;

  public static Content of(String activity, String learned,
      String encouragement, String imageUrl) {
    return new Content(activity, learned, encouragement, imageUrl);
  }
}
