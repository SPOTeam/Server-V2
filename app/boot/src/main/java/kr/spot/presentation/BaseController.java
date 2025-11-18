package kr.spot.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.code.status.SuccessStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "기본")
@RestController
@RequestMapping("/api")
public class BaseController {

    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/health-check")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, "OK"));
    }

    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/exception-health-check")
    public ResponseEntity<ApiResponse<String>> exceptionHealthCheck() {
        throw new IllegalStateException("Health Check Exception");
    }

    @GetMapping(value = "/current-env", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "[배포 관련] 무중단 배포를 위한 현재 환경 확인", description = """
            ## [배포 관련] 현재 서버의 환경을 확인합니다.
            현재 서버의 환경을 확인하여 무중단 배포를 위한 환경을 확인합니다.
            """)
    public String getCurrentEnvironment() {
        return System.getProperty("server.color");
    }
}