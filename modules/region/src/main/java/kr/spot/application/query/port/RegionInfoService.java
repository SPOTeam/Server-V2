package kr.spot.application.query.port;

import kr.spot.domain.Region;
import kr.spot.infrastructure.jpa.RegionRepository;
import kr.spot.ports.RegionInfoPort;
import kr.spot.ports.dto.RegionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RegionInfoService implements RegionInfoPort {

  private final RegionRepository regionRepository;

  @Override
  public boolean exists(String regionCode) {
    return regionRepository.existsById(regionCode);
  }

  @Override
  public RegionInfo getRegionInfoByCode(String regionCode) {
    Region region = regionRepository.getRegionById(regionCode);
    return RegionInfo.of(region.getProvince(), region.getDistrict(), region.getNeighborhood());
  }
}
