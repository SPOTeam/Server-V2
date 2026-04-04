# CLAUDE.md

이 파일은 Claude Code(claude.ai/code)가 이 저장소의 코드를 다룰 때 참고하는 가이드입니다.

## 빌드 명령어

```bash
# 프로젝트 빌드 (테스트 제외)
./gradlew clean build -x test

# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트 실행
./gradlew :modules:auth:test
./gradlew :modules:member:test

# 특정 테스트 클래스 실행
./gradlew :modules:auth:test --tests "kr.spot.infrastructure.jwt.JwtTokenProviderTest"

# 로컬 애플리케이션 실행
./gradlew :app:boot:bootRun
```

## 프로젝트 아키텍처

**Spring Boot 3.3.2** 기반 멀티모듈 Gradle 프로젝트, **Java 21** 사용.

### 모듈 구조

```
spot/
├── app/
│   ├── boot              # 메인 애플리케이션 진입점, 컨트롤러, 예외 처리
│   └── config            # Spring 설정 (Security, JPA, Swagger, Feign, S3)
├── common/
│   ├── api               # 공통 API 응답 클래스 (ApiResponse, 에러 코드)
│   ├── bucket            # 파일 저장소 포트 인터페이스
│   ├── snowflake         # 분산 ID 생성기
│   └── view              # 조회수 카운터 및 어뷰징 방지 포트 인터페이스
├── modules/
│   ├── shared            # 공용 enum (LoginType, Status)
│   ├── auth              # 인증 (OAuth, JWT, Security 필터)
│   ├── auth-api          # 인증 모듈 공개 인터페이스 (@CurrentMember 어노테이션)
│   ├── member            # 회원 도메인 및 서비스
│   ├── member-api        # 회원 모듈 공개 포트
│   ├── study             # 스터디 도메인 (일정, 회고록, 게시글, 할일 하위 도메인)
│   ├── study-api         # 스터디 모듈 공개 포트 및 이벤트
│   ├── post              # 게시글 도메인 (Redis 핫 게시글, 조회수)
│   ├── notification      # 푸시 알림 도메인
│   ├── notification-api  # 알림 모듈 공개 이벤트
│   ├── region            # 지역 도메인
│   └── region-api        # 지역 모듈 공개 포트
└── infra/
    ├── bucket-s3         # S3 기반 FileStoragePort 구현체
    └── view-redis        # Redis 기반 ViewCounter/ViewAbuseGuard 구현체
```

### 핵심 아키텍처 패턴

**포트 기반 모듈 간 통신**: 모듈은 `-api` 모듈을 통해 포트 인터페이스를 공개한다. 예를 들어
`member-api`는 `EnsureMemberFromOAuthPort`, `GetWriterInfoPort` 등을 정의하고, `member` 모듈이
이를 구현한다. 이를 통해 `auth`같은 모듈이 `member`에 직접 의존하지 않고 `member-api`에만 의존한다.

**이벤트 기반 모듈 간 통신**: Spring 이벤트를 통해 비동기 통신을 수행한다. 이벤트는 `-api` 모듈에
정의한다 (예: `study-api`의 `StudyApplicationProcessedEvent`, `notification-api`의
`NotificationRequestedEvent`).

**모듈 내 패키지 구조**:

- `domain/` - JPA 엔티티 및 값 객체
- `application/` - 서비스 (`command/`와 `query/`로 분리)
- `infrastructure/` - JPA 리포지토리, 외부 클라이언트, Redis 어댑터
- `presentation/` - REST 컨트롤러 (`command/`와 `query/`로 분리, 각각 DTO 포함)

**표준 API 응답**: 모든 엔드포인트는 `common:api`의 `ApiResponse<T>`를 반환한다.
성공: `ApiResponse.onSuccess(SuccessStatus._OK, result)`,
실패: `ApiResponse.onFailure(code, message, data)`.

**인증**: JWT 토큰 기반. 컨트롤러 메서드에 `@CurrentMember @Parameter(hidden = true) Long memberId`를
사용하여 인증된 사용자 ID를 주입한다. Security 필터 체인은 `app:config`에서 설정.

**QueryDSL**: 복잡한 쿼리를 사용하는 모듈(`study`, `post`)에 QueryDSL 설정이 포함되어 있다.
생성된 Q-class는 `build/generated/querydsl`에 위치.

**도메인 격리 규칙**:

- 모듈 간 엔티티/클래스 직접 참조 금지 - `-api` 포트를 사용
- 도메인 간 JOIN 금지 - 다른 도메인은 ID(PK)로만 참조

### 데이터베이스 및 외부 서비스

- **MySQL**: 주요 데이터베이스
- **Redis**: `post` 모듈에서 핫 게시글, 조회수, 어뷰징 방지에 사용
- **S3**: `FileStoragePort` 인터페이스를 통한 파일 업로드
- **OAuth**: Feign 클라이언트를 통한 카카오/네이버 로그인

## 코딩 컨벤션

- 서비스 레이어 메서드 시그니처에서 래퍼 클래스(`Long`, `Integer`) 대신 원시 타입(`long`, `int`)을
  사용하여 불필요한 박싱/언박싱을 방지한다.
- Tell, Don't Ask 원칙을 엄격히 따른다. 서비스가 엔티티에게 데이터를 요청하여 외부에서 결정하지 않고,
  엔티티에게 행동을 지시한다.
- **DTO 숫자 타입**: Response DTO에서 ID/PK는 `Long`(래퍼, JavaScript 정밀도를 위해 String 직렬화),
  카운트/통계는 `long`(원시 타입, 숫자로 유지).

## Git 컨벤션

- 커밋 메시지 형식: `[타입] 설명` (한국어)
- 타입: `Feature`, `Fix`, `Refactor`, `Docs`, `Merge`, `Test`
- 브랜치 전략: feature 브랜치에서 `develop`으로 PR을 통해 머지
- 예시: `[Feature] 출석체크 재시작 시 출석 레코드 중복 생성 방지`
