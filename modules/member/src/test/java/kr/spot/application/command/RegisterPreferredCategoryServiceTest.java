package kr.spot.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.association.PreferredCategory;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.PreferredCategoryRepository;
import kr.spot.ports.CategoryCatalogPort;
import kr.spot.presentation.command.dto.request.RegisterPreferredCategoryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

class RegisterPreferredCategoryServiceTest {

  private IdGenerator idGenerator;
  private CategoryCatalogPort categoryCatalogPort;
  private PreferredCategoryRepository preferredCategoryRepository;
  private RegisterPreferredCategoryService sut;

  @BeforeEach
  void setUp() {
    idGenerator = mock(IdGenerator.class);
    categoryCatalogPort = mock(CategoryCatalogPort.class);
    preferredCategoryRepository = mock(PreferredCategoryRepository.class);

    sut = new RegisterPreferredCategoryService(
        idGenerator, categoryCatalogPort, preferredCategoryRepository
    );
  }

  @Test
  @DisplayName("모든 카테고리가 유효하면 기존 선호를 삭제하고 새로 저장해야 한다")
  void should_delete_previous_and_save_new_preferred_categories_when_all_categories_are_valid() {
    // given
    Long memberId = 123L;
    List<String> categories = List.of("ALG", "LANG", "JOB");

    RegisterPreferredCategoryRequest request = mock(RegisterPreferredCategoryRequest.class);
    when(request.categories()).thenReturn(categories);

    when(categoryCatalogPort.exists("ALG")).thenReturn(true);
    when(categoryCatalogPort.exists("LANG")).thenReturn(true);
    when(categoryCatalogPort.exists("JOB")).thenReturn(true);

    when(idGenerator.nextId()).thenReturn(100L, 101L, 102L);

    // when
    sut.process(memberId, request);

    // then
    InOrder inOrder = Mockito.inOrder(preferredCategoryRepository, categoryCatalogPort);
    inOrder.verify(preferredCategoryRepository).deleteAllByMemberId(memberId);

    verify(categoryCatalogPort).exists("ALG");
    verify(categoryCatalogPort).exists("LANG");
    verify(categoryCatalogPort).exists("JOB");

    ArgumentCaptor<List<PreferredCategory>> listCaptor = ArgumentCaptor.forClass(List.class);
    verify(preferredCategoryRepository).saveAll(listCaptor.capture());

    assertThat(listCaptor.getValue()).hasSize(3);
    verify(idGenerator, times(3)).nextId();
  }

  @Test
  @DisplayName("유효하지 않은 카테고리가 포함되면 예외가 발생하고 저장하지 않는다")
  void should_throw_exception_and_not_save_when_invalid_category_exists() {
    // given
    Long memberId = 1L;
    List<String> categories = List.of("ALG", "INVALID");

    RegisterPreferredCategoryRequest request = mock(RegisterPreferredCategoryRequest.class);
    when(request.categories()).thenReturn(categories);

    when(categoryCatalogPort.exists("ALG")).thenReturn(true);
    when(categoryCatalogPort.exists("INVALID")).thenReturn(false);

    // when
    GeneralException ex = assertThrows(GeneralException.class,
        () -> sut.process(memberId, request));

    // then
    assertThat(ex.getStatus()).isEqualTo(ErrorStatus._NO_SUCH_CATEGORY);

    verify(preferredCategoryRepository).deleteAllByMemberId(memberId);
    verify(preferredCategoryRepository, never()).saveAll(anyList());
    verify(idGenerator, never()).nextId();
  }

  @Test
  @DisplayName("카테고리 목록이 비어있으면 기존 선호만 삭제하고 빈 목록을 저장해야 한다")
  void should_delete_previous_and_save_empty_list_when_categories_empty() {
    // given
    Long memberId = 77L;
    RegisterPreferredCategoryRequest request = mock(RegisterPreferredCategoryRequest.class);
    when(request.categories()).thenReturn(List.of());

    // when
    sut.process(memberId, request);

    // then
    verify(preferredCategoryRepository).deleteAllByMemberId(memberId);

    ArgumentCaptor<List<PreferredCategory>> listCaptor = ArgumentCaptor.forClass(List.class);
    verify(preferredCategoryRepository).saveAll(listCaptor.capture());

    assertThat(listCaptor.getValue()).isEmpty();
    verify(categoryCatalogPort, never()).exists(anyString());
    verify(idGenerator, never()).nextId();
  }
}
