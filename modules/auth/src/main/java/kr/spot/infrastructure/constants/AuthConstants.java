package kr.spot.infrastructure.constants;

public abstract class AuthConstants {

  public static final String CONTENT_TYPE = "application/x-www-form-urlencoded;charset=utf-8";
  public static final String APPLICATION_JSON = "application/json";
  public static final String GRANT_TYPE = "grant_type";
  public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

  public static final String CLIENT_ID = "client_id";
  public static final String REDIRECT_URI = "redirect_uri";
  public static final String RESPONSE_TYPE = "response_type";
  public static final String RESPONSE_TYPE_CODE = "code";
  public static final String QUERY_DELIMITER = "&";
  public static final String KEY_VALUE_DELIMITER = "=";
  public static final String QUERY_PREFIX = "?";
  public static final String SCOPE = "scope";
  public static final String STATE = "state";
  public static final String STATE_STRING = "STATE_STRING";
  public static final String CLIENT_SECRET = "client_secret";

  public static final String HEADER_AUTHORIZATION = "Authorization";
  public static final String KAKAO_AUTHORIZATION = "KakaoAK ";
  public static final String HEADER_CONTENT_TYPE = "Content-Type";

  public static final String EMAIL = "email";
}
