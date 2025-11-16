package kr.spot.ports;

import java.util.List;

public interface GetPreferredCategoryPort {

    List<String> get(Long viewerId);
}
