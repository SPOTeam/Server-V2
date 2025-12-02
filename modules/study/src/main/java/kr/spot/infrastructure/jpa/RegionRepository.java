package kr.spot.infrastructure.jpa;

import kr.spot.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
  
}
