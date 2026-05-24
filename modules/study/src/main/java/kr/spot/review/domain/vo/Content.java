package kr.spot.review.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.ArrayList;
import java.util.List;
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
  @Column(columnDefinition = "TEXT")
  private String activity;

  // 오늘 새롭게 배운 점은 무엇인가요?
  @Column(columnDefinition = "TEXT")
  private String learned;

  // 고생한 나에게 한마디
  @Column(columnDefinition = "TEXT")
  private String encouragement;

  private String imageUrl;
  private String imageUrl2;
  private String imageUrl3;

  public static Content of(String activity, String learned,
      String encouragement, List<String> imageUrls) {
    List<String> safeImageUrls = imageUrls == null ? List.of() : imageUrls;
    return new Content(
        activity,
        learned,
        encouragement,
        getImageUrlByIndex(safeImageUrls, 0),
        getImageUrlByIndex(safeImageUrls, 1),
        getImageUrlByIndex(safeImageUrls, 2)
    );
  }

  public List<String> getImageUrls() {
    List<String> imageUrls = new ArrayList<>();
    if (imageUrl != null) {
      imageUrls.add(imageUrl);
    }
    if (imageUrl2 != null) {
      imageUrls.add(imageUrl2);
    }
    if (imageUrl3 != null) {
      imageUrls.add(imageUrl3);
    }
    return List.copyOf(imageUrls);
  }

  private static String getImageUrlByIndex(List<String> imageUrls, int index) {
    if (index >= imageUrls.size()) {
      return null;
    }
    return imageUrls.get(index);
  }
}
