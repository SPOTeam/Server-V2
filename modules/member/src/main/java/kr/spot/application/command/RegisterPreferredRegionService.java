package kr.spot.application.command;

import java.util.List;
import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.association.PreferredRegion;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.PreferredRegionRepository;
import kr.spot.ports.RegionInfoPort;
import kr.spot.presentation.command.dto.request.RegisterPreferredRegionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterPreferredRegionService {

  private final IdGenerator idGenerator;
  private final RegionInfoPort regionInfoPort;
  private final PreferredRegionRepository preferredRegionRepository;

  public void process(Long memberId, RegisterPreferredRegionRequest request) {
    deleteAllPreviousPreferredRegions(memberId);
    validateIsValidRegionCode(request);

    List<PreferredRegion> list = getPreferredCategoryList(memberId, request);
    preferredRegionRepository.saveAll(list);
  }

  private List<PreferredRegion> getPreferredCategoryList(Long memberId,
      RegisterPreferredRegionRequest request) {
    return request.regionCodes().stream()
        .map((regionCode) -> PreferredRegion.of(idGenerator.nextId(), memberId, regionCode))
        .toList();
  }

  private void validateIsValidRegionCode(RegisterPreferredRegionRequest request) {
    request.regionCodes().forEach(
        regionCode -> {
          if (!regionInfoPort.exists(regionCode)) {
            throw new GeneralException(ErrorStatus._NO_SUCH_REGION);
          }
        }
    );
  }

  private void deleteAllPreviousPreferredRegions(Long memberId) {
    preferredRegionRepository.deleteAllByMemberId(memberId);
  }
}
