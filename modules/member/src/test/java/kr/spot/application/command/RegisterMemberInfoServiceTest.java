package kr.spot.application.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.MemberRepository;
import kr.spot.presentation.command.dto.request.UpdateMemberNameRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegisterMemberInfoServiceTest {

  private MemberRepository memberRepository;
  private RegisterMemberInfoService sut;

  @BeforeEach
  void setUp() {
    memberRepository = mock(MemberRepository.class);
    sut = new RegisterMemberInfoService(memberRepository);
  }

  @Test
  @DisplayName("회원 이름 업데이트 성공 시 예외가 발생하지 않아야 한다")
  void should_update_member_name_when_member_exists() {
    // given
    Long memberId = 1L;
    UpdateMemberNameRequest request = new UpdateMemberNameRequest("newName");

    when(memberRepository.updateNameById(memberId, request.name())).thenReturn(1);

    // when
    sut.updateMemberName(memberId, request);

    // then
    verify(memberRepository).updateNameById(memberId, request.name());
  }

  @Test
  @DisplayName("회원이 존재하지 않으면 GeneralException을 던져야 한다")
  void should_throw_exception_when_member_does_not_exist() {
    // given
    Long memberId = 99L;
    UpdateMemberNameRequest request = new UpdateMemberNameRequest("newName");

    when(memberRepository.updateNameById(memberId, request.name())).thenReturn(0);

    // when & then
    assertThatThrownBy(() -> sut.updateMemberName(memberId, request))
        .isInstanceOf(GeneralException.class);

    verify(memberRepository).updateNameById(memberId, request.name());
  }
}
