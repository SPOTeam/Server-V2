package kr.spot.infrastructure.jpa;

import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.Region;
import kr.spot.exception.GeneralException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, String> {

  default Region getRegionById(String id) {
    return findById(id).orElseThrow(() -> new GeneralException(ErrorStatus._REGION_NOT_FOUND));
  }
}
