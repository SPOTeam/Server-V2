package kr.spot.infrastructure.security.filters;

import static kr.spot.infrastructure.constants.JwtConstants.AUTHORIZATION_HEADER;
import static kr.spot.infrastructure.constants.JwtConstants.BEARER_PREFIX;
import static kr.spot.infrastructure.constants.JwtConstants.ROLE_PREFIX;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import kr.spot.application.token.TokenProvider;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.constants.SecurityWhitelist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  public static final String USER = "user";
  
  private final TokenProvider tokenProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    if (isPermitAllRequest(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    authenticateRequest(request);
    filterChain.doFilter(request, response);
  }

  private void authenticateRequest(HttpServletRequest request) {
    long memberId = tokenProvider.getMemberIdByToken(extractToken(request));

    var authority = new SimpleGrantedAuthority(ROLE_PREFIX + USER);
    var authentication = new UsernamePasswordAuthenticationToken(memberId, null,
        List.of(authority));
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private String extractToken(HttpServletRequest request) {
    String bearer = request.getHeader(AUTHORIZATION_HEADER);
    if (!StringUtils.hasText(bearer)) {
      throw new GeneralException(ErrorStatus._NO_AUTHORIZED);
    }
    if (!bearer.startsWith(BEARER_PREFIX)) {
      throw new GeneralException(ErrorStatus._INVALID_JWT);
    }
    String token = bearer.substring(BEARER_PREFIX.length());
    if (!StringUtils.hasText(token)) {
      throw new GeneralException(ErrorStatus._EMPTY_JWT);
    }
    return token;
  }


  private boolean isPermitAllRequest(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return Stream.of(SecurityWhitelist.EXACT_MATCH).anyMatch(uri::equals)
        || Stream.of(SecurityWhitelist.PREFIX_MATCH).anyMatch(uri::startsWith)
        || Stream.of(SecurityWhitelist.REGEX_MATCH).anyMatch(uri::matches);
  }
}
