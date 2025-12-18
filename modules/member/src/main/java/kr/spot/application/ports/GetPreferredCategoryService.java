package kr.spot.application.ports;

import java.util.List;
import kr.spot.domain.association.PreferredCategory;
import kr.spot.infrastructure.jpa.PreferredCategoryRepository;
import kr.spot.ports.GetPreferredCategoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetPreferredCategoryService implements GetPreferredCategoryPort {

  private final PreferredCategoryRepository preferredCategoryRepository;

  @Override
  public List<String> get(Long viewerId) {
    return preferredCategoryRepository.findAllByMemberId(viewerId)
        .stream()
        .map(PreferredCategory::getCategory)
        .toList();
  }
}
