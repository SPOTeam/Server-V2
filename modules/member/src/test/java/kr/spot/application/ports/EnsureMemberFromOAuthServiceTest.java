package kr.spot.application.ports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.IdGenerator;
import kr.spot.common.fixture.MemberFixture;
import kr.spot.domain.Member;
import kr.spot.domain.enums.LoginType;
import kr.spot.infrastructure.jpa.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnsureMemberFromOAuthServiceTest {

  @Mock
  MemberRepository memberRepository;
  @Mock
  IdGenerator idGenerator;

  @InjectMocks
  EnsureMemberFromOAuthService service;

  @Test
  @DisplayName("존재하지 않으면 새 회원을 생성하고 ID를 반환한다")
  void ensure_creates_when_not_exists() {
    // given
    when(memberRepository.existsByEmailAndLoginType(MemberFixture.email(), LoginType.KAKAO))
        .thenReturn(false);
    when(memberRepository.save(any(Member.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // when
    long returnedId = service.ensure(
        LoginType.KAKAO.name(),
        MemberFixture.EMAIL,
        MemberFixture.NAME,
        MemberFixture.PROFILE_IMAGE
    );

    // then
    ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
    verify(memberRepository).save(captor.capture());
    Member saved = captor.getValue();

    assertThat(saved.getEmail()).isEqualTo(MemberFixture.email());
    assertThat(saved.getLoginType()).isEqualTo(LoginType.KAKAO);
    assertThat(saved.getName()).isEqualTo(MemberFixture.NAME);
    assertThat(saved.getProfileImageUrl()).isEqualTo(MemberFixture.PROFILE_IMAGE);

    assertThat(returnedId).isEqualTo(saved.getId());
  }
}
