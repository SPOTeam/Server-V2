package kr.spot.domain.enums;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;

public enum Category {

  LANGUAGE,            // 어학
  CERTIFICATION,       // 자격증
  CAREER,              // 취업
  CURRENT_AFFAIRS,     // 시사뉴스
  SELF_STUDY,          // 자율학습
  DEBATE,              // 토론
  PROJECT,             // 프로젝트
  COMPETITION,         // 공모전
  MAJOR_CAREER,        // 전공및진로학습
  OTHER;                // 기타

  public static boolean contains(String categoryName) {
    for (Category c : Category.values()) {
      if (c.name().equals(categoryName)) {
        return true;
      }
    }
    return false;
  }

  public static Category fromString(String categoryName) {
    for (Category c : Category.values()) {
      if (c.name().equals(categoryName)) {
        return c;
      }
    }
    throw new GeneralException(ErrorStatus._NO_SUCH_CATEGORY);
  }
}
