package kr.spot.application.ports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.spot.IdGenerator;
import kr.spot.common.fixture.MemberFixture;
import kr.spot.domain.Member;
import kr.spot.domain.enums.LoginType;
import kr.spot.domain.enums.Status;
import kr.spot.infrastructure.jpa.MemberRepository;
import kr.spot.ports.dto.EnsureResult;
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
  @DisplayName("존재하지 않으면 새 회원을 생성하고 isNew=true 로 반환한다")
  void ensure_creates_when_not_exists() {
    when(memberRepository.findByEmailAndLoginTypeIncludingInactive(
        MemberFixture.EMAIL, LoginType.KAKAO.name()))
        .thenReturn(Optional.empty());
    when(memberRepository.save(any(Member.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    EnsureResult result = service.ensure(
        LoginType.KAKAO.name(),
        MemberFixture.EMAIL,
        MemberFixture.NAME,
        MemberFixture.PROFILE_IMAGE
    );

    ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
    verify(memberRepository).save(captor.capture());
    Member saved = captor.getValue();

    assertThat(saved.getEmail()).isEqualTo(MemberFixture.email());
    assertThat(saved.getLoginType()).isEqualTo(LoginType.KAKAO);
    assertThat(saved.getName()).isEqualTo(MemberFixture.NAME);
    assertThat(saved.getProfileImageUrl()).isEqualTo(MemberFixture.PROFILE_IMAGE);

    assertThat(result.memberId()).isEqualTo(saved.getId());
    assertThat(result.isNew()).isTrue();
  }

  @Test
  @DisplayName("ACTIVE 회원이 존재하면 기존 회원을 재사용하고 isNew=false 로 반환한다")
  void ensure_reuses_when_active_exists() {
    Member existing = MemberFixture.member();
    when(memberRepository.findByEmailAndLoginTypeIncludingInactive(
        MemberFixture.EMAIL, LoginType.KAKAO.name()))
        .thenReturn(Optional.of(existing));

    EnsureResult result = service.ensure(
        LoginType.KAKAO.name(),
        MemberFixture.EMAIL,
        "다른이름",
        "다른이미지"
    );

    verify(memberRepository, never()).save(any(Member.class));
    assertThat(result.memberId()).isEqualTo(existing.getId());
    assertThat(result.isNew()).isFalse();
    assertThat(existing.getName()).isEqualTo(MemberFixture.NAME);
  }

  @Test
  @DisplayName("INACTIVE(탈퇴) 회원이 존재하면 재활성화하고 isNew=true 로 반환한다")
  void ensure_reactivates_when_inactive_exists() {
    Member withdrawn = MemberFixture.member();
    withdrawn.delete();

    when(memberRepository.findByEmailAndLoginTypeIncludingInactive(
        MemberFixture.EMAIL, LoginType.KAKAO.name()))
        .thenReturn(Optional.of(withdrawn));

    EnsureResult result = service.ensure(
        LoginType.KAKAO.name(),
        MemberFixture.EMAIL,
        "새이름",
        "새이미지"
    );

    verify(memberRepository, never()).save(any(Member.class));
    assertThat(result.memberId()).isEqualTo(withdrawn.getId());
    assertThat(result.isNew()).isTrue();
    assertThat(withdrawn.getStatus()).isEqualTo(Status.ACTIVE);
    assertThat(withdrawn.getName()).isEqualTo("새이름");
    assertThat(withdrawn.getProfileImageUrl()).isEqualTo("새이미지");
  }
}
