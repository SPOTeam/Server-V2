package kr.spot.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.util.Date;
import kr.spot.application.token.TokenProvider;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.presentation.command.dto.TokenDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider implements TokenProvider {

  public static final String MEMBER_ID = "memberId";
  public static final String TOKEN_TYPE = "tokenType";
  public static final String ACCESS = "access";
  public static final String REFRESH = "refresh";

  private final String JWT_SECRET_KEY;
  private final Long ACCESS_TOKEN_EXPIRATION_TIME;
  private final Long REFRESH_TOKEN_EXPIRATION_TIME;

  public JwtTokenProvider(
      @Value("${token.access_secret}") final String JWT_SECRET_KEY,
      @Value("${token.access_token_expiration_time}") final Long ACCESS_TOKEN_EXPIRATION_TIME,
      @Value("${token.refresh_token_expiration_time}") final Long REFRESH_TOKEN_EXPIRATION_TIME
  ) {
    this.JWT_SECRET_KEY = JWT_SECRET_KEY;
    this.ACCESS_TOKEN_EXPIRATION_TIME = ACCESS_TOKEN_EXPIRATION_TIME;
    this.REFRESH_TOKEN_EXPIRATION_TIME = REFRESH_TOKEN_EXPIRATION_TIME;
  }

  @Override
  public TokenDTO createToken(final Long memberId) {
    Date now = new Date();
    String accessToken = generateToken(memberId, now, ACCESS_TOKEN_EXPIRATION_TIME,
        ACCESS); // 액세스 토큰 생성
    String refreshToken = generateToken(memberId, now, REFRESH_TOKEN_EXPIRATION_TIME,
        REFRESH); // 리프레시 토큰 생성

    // 토큰 DTO 반환
    return TokenDTO.of(accessToken, refreshToken);
  }

  private String generateToken(final Long memberId, Date now, final long expirationTime,
      final String tokenType) {
    return Jwts.builder()
        .claim(MEMBER_ID, memberId) // 회원 ID
        .claim(TOKEN_TYPE, tokenType) // 토큰 타입
        .setIssuedAt(now) // 발급 시간
        .setExpiration(new Date(now.getTime() + expirationTime)) // 만료 시간
        .signWith(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
        .compact();
  }

  @Override
  public void validateToken(final String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()))
          .build()
          .parseClaimsJws(token);
    } catch (SecurityException | MalformedJwtException e) {
      throw new GeneralException(ErrorStatus._INVALID_JWT);
    } catch (ExpiredJwtException e) {
      throw new GeneralException(ErrorStatus._EXPIRED_JWT);
    } catch (UnsupportedJwtException | SignatureException e) {
      throw new GeneralException(ErrorStatus._UNSUPPORTED_JWT);
    } catch (IllegalArgumentException e) {
      throw new GeneralException(ErrorStatus._EMPTY_JWT);
    }
  }

  @Override
  public Long getMemberIdByToken(final String token) {
    Claims claims = getClaims(token);
    return claims.get(MEMBER_ID, Long.class);
  }

  public TokenDTO createTestToken(final Long memberId) {
    Date now = new Date();
    long oneYearInMillis = 365L * 24 * 60 * 60 * 1000;
    String accessToken = generateToken(memberId, now, oneYearInMillis, ACCESS);
    String refreshToken = generateToken(memberId, now, oneYearInMillis, REFRESH);
    return TokenDTO.of(accessToken, refreshToken);
  }

  private Claims getClaims(final String token) {
    return Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()))
        .build()
        .parseClaimsJws(token)
        .getBody();
  }
}
