package kr.spot.study.application.query.port;

import kr.spot.ports.CategoryCatalogPort;
import kr.spot.study.domain.enums.Category;
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
