package kr.spot.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.spot.annotations.CurrentMember;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class CurrentMemberArgumentResolverTest {

  CurrentMemberArgumentResolver resolver;

  @BeforeEach
  void setUp() {
    resolver = new CurrentMemberArgumentResolver();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private MethodParameter param(String methodName, int index) throws NoSuchMethodException {
    var m = DummyController.class.getMethod(methodName, Long.class); // 기본은 Long
    return new MethodParameter(m, index);
  }

  private MethodParameter paramNotLong() throws NoSuchMethodException {
    var m = DummyController.class.getMethod("withAnnoNotLong", String.class);
    return new MethodParameter(m, 0);
  }

  @Test
  @DisplayName("supportsParameter() - CurrentMember 어노테이션 있고, 타입이 Long이면 true")
  void should_support_when_parameter_has_CurrentMember_and_is_Long() throws Exception {
    var p = param("withAnno", 0);
    assertThat(resolver.supportsParameter(p)).isTrue();
  }

  @Test
  @DisplayName("supportsParameter() - CurrentMember 어노테이션 없으면 false")
  void should_not_support_when_missing_annotation() throws Exception {
    var p = param("withoutAnno", 0);
    assertThat(resolver.supportsParameter(p)).isFalse();
  }

  @Test
  @DisplayName("supportsParameter() - 타입이 Long이 아니면 false")
  void should_not_support_when_type_is_not_Long() throws Exception {
    var p = paramNotLong();
    assertThat(resolver.supportsParameter(p)).isFalse();
  }

  @Test
  @DisplayName("resolveArgument() - SecurityContext에서 memberId 추출")
  void should_resolve_member_id_from_security_context_when_principal_is_long() throws Exception {
    // given: SecurityContext에 principal = 123L
    var auth = new UsernamePasswordAuthenticationToken(123L, null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    var p = param("withAnno", 0);

    // when
    Object resolved = resolver.resolveArgument(p, null, null, null);

    // then
    assertThat(resolved).isEqualTo(123L);
  }

  @Test
  @DisplayName("resolveArgument() - principal이 String 숫자여도 Long으로 변환 가능")
  void should_resolve_member_id_when_principal_is_string_numeric() throws Exception {
    // given: principal이 "123" 문자열이어도 toString → parseLong 가능
    var auth = new UsernamePasswordAuthenticationToken("123", null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    var p = param("withAnno", 0);

    Object resolved = resolver.resolveArgument(p, null, null, null);

    assertThat(resolved).isEqualTo(123L);
  }

  @Test
  @DisplayName("resolveArgument() - SecurityContext에 인증 정보가 없으면 예외")
  void should_throw_when_authentication_is_null() throws Exception {
    var p = param("withAnno", 0);

    assertThatThrownBy(() -> resolver.resolveArgument(p, null, null, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("resolveArgument() - principal이 숫자가 아니면 예외")
  void should_throw_when_principal_is_not_numeric() throws Exception {
    var auth = new UsernamePasswordAuthenticationToken("abc", null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    var p = param("withAnno", 0);

    assertThatThrownBy(() -> resolver.resolveArgument(p, null, null, null))
        .isInstanceOf(NumberFormatException.class);
  }

  // --- 테스트용 가짜 컨트롤러 메서드들 ---
  static class DummyController {

    public void withAnno(@CurrentMember Long memberId) {
    }

    public void withoutAnno(Long memberId) {
    }

    public void withAnnoNotLong(@CurrentMember String memberId) {
    }
  }
}
