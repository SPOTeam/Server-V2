package kr.spot.application.ports;

import java.util.List;
import kr.spot.domain.enums.HotPostSortBy;

public interface HotPostStore {

  void replaceTop3(List<Long> postIds, String key);

  List<Long> getTop3(HotPostSortBy sortBy);
}
