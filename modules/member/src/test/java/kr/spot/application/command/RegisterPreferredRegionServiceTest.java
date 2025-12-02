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
import kr.spot.domain.association.PreferredRegion;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.PreferredRegionRepository;
import kr.spot.ports.RegionInfoPort;
import kr.spot.presentation.command.dto.request.RegisterPreferredRegionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

class RegisterPreferredRegionServiceTest {

  private IdGenerator idGenerator;
  private RegionInfoPort regionInfoPort;
  private PreferredRegionRepository preferredRegionRepository;
  private RegisterPreferredRegionService sut;

  @BeforeEach
  void setUp() {
    idGenerator = mock(IdGenerator.class);
    regionInfoPort = mock(RegionInfoPort.class);
    preferredRegionRepository = mock(PreferredRegionRepository.class);

    sut = new RegisterPreferredRegionService(
        idGenerator, regionInfoPort, preferredRegionRepository
    );
  }

  @Test
  @DisplayName("모든 지역 코드가 유효하면 기존 선호를 삭제하고 새로 저장해야 한다")
  void should_delete_previous_and_save_new_preferred_regions_when_all_regions_are_valid() {
    // given
    Long memberId = 123L;
    List<String> regionCodes = List.of("1111051500", "1111053000", "1111054000");

    RegisterPreferredRegionRequest request = mock(RegisterPreferredRegionRequest.class);
    when(request.regionCodes()).thenReturn(regionCodes);

    when(regionInfoPort.exists("1111051500")).thenReturn(true);
    when(regionInfoPort.exists("1111053000")).thenReturn(true);
    when(regionInfoPort.exists("1111054000")).thenReturn(true);

    when(idGenerator.nextId()).thenReturn(100L, 101L, 102L);

    // when
    sut.process(memberId, request);

    // then
    InOrder inOrder = Mockito.inOrder(preferredRegionRepository, regionInfoPort);
    inOrder.verify(preferredRegionRepository).deleteAllByMemberId(memberId);

    verify(regionInfoPort).exists("1111051500");
    verify(regionInfoPort).exists("1111053000");
    verify(regionInfoPort).exists("1111054000");

    ArgumentCaptor<List<PreferredRegion>> listCaptor = ArgumentCaptor.forClass(List.class);
    verify(preferredRegionRepository).saveAll(listCaptor.capture());

    assertThat(listCaptor.getValue()).hasSize(3);
    verify(idGenerator, times(3)).nextId();
  }

  @Test
  @DisplayName("유효하지 않은 지역 코드가 포함되면 예외가 발생하고 저장하지 않는다")
  void should_throw_exception_and_not_save_when_invalid_region_exists() {
    // given
    Long memberId = 1L;
    List<String> regionCodes = List.of("1111051500", "INVALID_CODE");

    RegisterPreferredRegionRequest request = mock(RegisterPreferredRegionRequest.class);
    when(request.regionCodes()).thenReturn(regionCodes);

    when(regionInfoPort.exists("1111051500")).thenReturn(true);
    when(regionInfoPort.exists("INVALID_CODE")).thenReturn(false);

    // when
    GeneralException ex = assertThrows(GeneralException.class,
        () -> sut.process(memberId, request));

    // then
    assertThat(ex.getStatus()).isEqualTo(ErrorStatus._NO_SUCH_REGION);

    verify(preferredRegionRepository).deleteAllByMemberId(memberId);
    verify(preferredRegionRepository, never()).saveAll(anyList());
    verify(idGenerator, never()).nextId();
  }

  @Test
  @DisplayName("지역 코드 목록이 비어있으면 기존 선호만 삭제하고 빈 목록을 저장해야 한다")
  void should_delete_previous_and_save_empty_list_when_regions_empty() {
    // given
    Long memberId = 77L;
    RegisterPreferredRegionRequest request = mock(RegisterPreferredRegionRequest.class);
    when(request.regionCodes()).thenReturn(List.of());

    // when
    sut.process(memberId, request);

    // then
    verify(preferredRegionRepository).deleteAllByMemberId(memberId);

    ArgumentCaptor<List<PreferredRegion>> listCaptor = ArgumentCaptor.forClass(List.class);
    verify(preferredRegionRepository).saveAll(listCaptor.capture());

    assertThat(listCaptor.getValue()).isEmpty();
    verify(regionInfoPort, never()).exists(anyString());
    verify(idGenerator, never()).nextId();
  }
}
