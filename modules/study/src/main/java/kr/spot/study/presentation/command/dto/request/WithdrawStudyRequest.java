package kr.spot.study.presentation.command.dto.request;

import kr.spot.study.domain.enums.WithdrawReason;

public record WithdrawStudyRequest(
    WithdrawReason withdrawReason,
    Long nextOwnerId
) {

}
