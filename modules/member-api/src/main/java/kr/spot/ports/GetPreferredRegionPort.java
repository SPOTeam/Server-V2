package kr.spot.ports;

import java.util.List;

public interface GetPreferredRegionPort {

    List<String> get(Long viewerId);
}
