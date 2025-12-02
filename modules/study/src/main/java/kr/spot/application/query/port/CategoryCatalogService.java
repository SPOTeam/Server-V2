package kr.spot.application.query.port;

import kr.spot.domain.enums.Category;
import kr.spot.ports.CategoryCatalogPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryCatalogService implements CategoryCatalogPort {

  @Override
  public boolean exists(String category) {
    return Category.contains(category);
  }
}
