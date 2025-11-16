package kr.spot.application.ports;

import java.util.List;
import kr.spot.domain.association.PreferredRegion;
import kr.spot.infrastructure.jpa.PreferredRegionRepository;
import kr.spot.ports.GetPreferredRegionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetPreferredRegionService implements GetPreferredRegionPort {

    private final PreferredRegionRepository preferredRegionRepository;

    @Override
    public List<String> get(Long viewerId) {
        return preferredRegionRepository.findAllByMemberId(viewerId)
                .stream()
                .map(PreferredRegion::getRegionCode)
                .toList();
    }
}
