package kr.spot.ports;

import kr.spot.ports.dto.RegionInfo;

public interface RegionInfoPort {

  boolean exists(String regionCode);

  RegionInfo getRegionInfoByCode(String regionCode);
}
