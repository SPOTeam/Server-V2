package kr.spot.infrastructure.constants;

public abstract class SecurityWhitelist {

  // 정확히 일치해야 하는 경로들
  public static final String[] EXACT_MATCH = {
      "/health",
      "/api/members",
      "/api/current-env",
      "/swagger-ui.html",
  };

  // 접두사로 매칭되는 경로들
  public static final String[] PREFIX_MATCH = {
      "/swagger-ui",
      "/v3/api-docs",
      "/docs",
      "/swagger-resources",
      "/webjars",
      "/api/oauth",
      "/api/auth",
      "/actuator/prometheus"
  };

  // 정규식으로 매칭되는 경로들
  public static final String[] REGEX_MATCH = {
  };

  private SecurityWhitelist() {
  }
}
