package kr.spot.infrastructure.security;

import static kr.spot.infrastructure.constants.AuthConstants.CONTENT_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.spot.ApiResponse;
import kr.spot.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException {
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(CONTENT_TYPE);
    String json = objectMapper.writeValueAsString(
        ApiResponse.onFailure(ErrorStatus._FORBIDDEN.getCode(), ErrorStatus._FORBIDDEN.getMessage(),
            null));
    response.getWriter().write(json);
  }
}
