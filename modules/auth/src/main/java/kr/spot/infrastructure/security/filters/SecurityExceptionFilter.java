package kr.spot.infrastructure.security.filters;

import static kr.spot.infrastructure.constants.AuthConstants.CONTENT_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.spot.ApiResponse;
import kr.spot.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityExceptionFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper;
  
  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    try {
      filterChain.doFilter(request, response);
    } catch (GeneralException ex) {
      log.warn("Security 관련 예외 발생: {}", ex.getMessage());
      sendErrorResponse(response, HttpStatus.UNAUTHORIZED, ex.getMessage());
    } catch (SecurityException ex) {
      log.warn("Security 관련 에러 발생 error: {}", ex.getMessage());
      sendErrorResponse(response, HttpStatus.FORBIDDEN, "접근이 거부되었습니다.");
    }
  }

  private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType(CONTENT_TYPE);
    String json = objectMapper.writeValueAsString(
        ApiResponse.onFailure(status.name(), message, null));
    response.getWriter().write(json);
  }
}
