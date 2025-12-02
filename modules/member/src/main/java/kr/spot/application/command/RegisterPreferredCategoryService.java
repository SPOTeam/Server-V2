package kr.spot.application.command;

import java.util.List;
import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.association.PreferredCategory;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.PreferredCategoryRepository;
import kr.spot.ports.CategoryCatalogPort;
import kr.spot.presentation.command.dto.request.RegisterPreferredCategoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterPreferredCategoryService {

  private final IdGenerator idGenerator;
  private final CategoryCatalogPort categoryCatalogPort;
  private final PreferredCategoryRepository preferredCategoryRepository;

  public void process(Long memberId, RegisterPreferredCategoryRequest request) {
    deleteAllPreviousPreferredCategory(memberId);
    validateIsValidCategoryName(request);

    List<PreferredCategory> list = getPreferredCategoryList(memberId, request);
    preferredCategoryRepository.saveAll(list);
  }

  private List<PreferredCategory> getPreferredCategoryList(Long memberId,
      RegisterPreferredCategoryRequest request) {
    return request.categories().stream()
        .map((category) -> PreferredCategory.of(idGenerator.nextId(), memberId, category))
        .toList();
  }

  private void validateIsValidCategoryName(RegisterPreferredCategoryRequest request) {
    request.categories().forEach(
        category -> {
          if (!categoryCatalogPort.exists(category)) {
            throw new GeneralException(ErrorStatus._NO_SUCH_CATEGORY);
          }
        }
    );
  }

  private void deleteAllPreviousPreferredCategory(Long memberId) {
    preferredCategoryRepository.deleteAllByMemberId(memberId);
  }
}
