package kr.spot.application.oauth;


import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import kr.spot.application.oauth.strategy.OAuthStrategy;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.enums.LoginType;
import kr.spot.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuthStrategyFactory {

  private final Map<LoginType, OAuthStrategy> strategyMap;

  public OAuthStrategyFactory(List<OAuthStrategy> strategies) {
    this.strategyMap = Collections.unmodifiableMap(
        strategies.stream().collect(
            Collectors.toMap(
                OAuthStrategy::getType,
                s -> s,
                (a, b) -> {
                  throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
                },
                () -> new EnumMap<>(LoginType.class)
            )
        )
    );
  }

  @PostConstruct
  void logRegistered() {
    log.info("Registered OAuth strategies: {}", strategyMap.keySet());
  }

  public OAuthStrategy getStrategy(LoginType type) {
    return Optional.ofNullable(strategyMap.get(type))
        .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_UNSUPPORTED_LOGIN_TYPE));
  }
}
