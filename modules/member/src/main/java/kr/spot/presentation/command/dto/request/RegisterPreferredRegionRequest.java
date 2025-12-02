package kr.spot.presentation.command.dto.request;

import java.util.List;

public record RegisterPreferredRegionRequest(
    List<String> regionCodes
) {

}
