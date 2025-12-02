package kr.spot.infrastructure.security.filters;

import static kr.spot.infrastructure.constants.JwtConstants.AUTHORIZATION_HEADER;
import static kr.spot.infrastructure.constants.JwtConstants.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import jakarta.servlet.ServletException;
import java.io.IOException;
import kr.spot.application.token.TokenProvider;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class JwtAuthenticationFilterTest {

  TokenProvider tokenProvider;
  JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    tokenProvider = Mockito.mock(TokenProvider.class);
    filter = new JwtAuthenticationFilter(tokenProvider);
  }

  @AfterEach
  void tearDown() {
    org.springframework.security.core.context.SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("화이트리스트 URI는 필터를 우회한다.")
  void should_bypass_filter_when_permit_all_uri() throws ServletException, IOException {
    var request = new MockHttpServletRequest("GET", "/health"); // 화이트리스트에 맞는 URI
    var response = new MockHttpServletResponse();
    var chain = new MockFilterChain();

    // 실행
    filter.doFilter(request, response, chain);

    // 인증 객체가 세팅되지 않아야 함
    assertThat(org.springframework.security.core.context.SecurityContextHolder.getContext()
        .getAuthentication())
        .isNull();
  }

  @Test
  @DisplayName("Authorization 헤더가 없으면 예외를 던진다.")
  void should_throw_when_no_authorization_header() {
    var request = new MockHttpServletRequest("GET", "/api/secure");
    var response = new MockHttpServletResponse();

    assertThatThrownBy(() -> filter.doFilter(request, response, new MockFilterChain()))
        .isInstanceOf(GeneralException.class)
        .hasMessageContaining(ErrorStatus._NO_AUTHORIZED.getCode());
  }

  @Test
  @DisplayName("Bearer 접두사가 아니면 예외를 던진다.")
  void should_throw_when_not_bearer() {
    var request = new MockHttpServletRequest("GET", "/api/secure");
    request.addHeader(AUTHORIZATION_HEADER, "Token abc");
    var response = new MockHttpServletResponse();

    assertThatThrownBy(() -> filter.doFilter(request, response, new MockFilterChain()))
        .isInstanceOf(GeneralException.class)
        .hasMessageContaining(ErrorStatus._INVALID_JWT.getCode());
  }

  @Test
  @DisplayName("토큰이 비어있으면 예외를 던진다.")
  void should_throw_when_empty_token() {
    var request = new MockHttpServletRequest("GET", "/api/secure");
    request.addHeader(AUTHORIZATION_HEADER, BEARER_PREFIX);
    var response = new MockHttpServletResponse();

    assertThatThrownBy(() -> filter.doFilter(request, response, new MockFilterChain()))
        .isInstanceOf(GeneralException.class)
        .hasMessageContaining(ErrorStatus._EMPTY_JWT.getCode());
  }

  @Test
  @DisplayName("유효한 토큰이면 Security Context에 인증 정보를 세팅한다.")
  void should_set_security_context_when_valid_token() throws Exception {
    // given
    var request = new MockHttpServletRequest("GET", "/api/secure");
    request.addHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + "valid.jwt.token");
    var response = new MockHttpServletResponse();
    var chain = new MockFilterChain();

    given(tokenProvider.getMemberIdByToken("valid.jwt.token")).willReturn(123L);

    // when
    filter.doFilter(request, response, chain);

    // then
    var auth = org.springframework.security.core.context.SecurityContextHolder.getContext()
        .getAuthentication();
    assertThat(auth).isNotNull();
    assertThat(auth.getPrincipal()).isEqualTo(123L);
    assertThat(auth.getAuthorities().stream().map(a -> a.getAuthority()).toList())
        .contains("ROLE_user");
    assertThat(auth.isAuthenticated()).isTrue();
  }
}
