package kr.spot.study.infrastructure.jpa;

import kr.spot.study.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {

}
