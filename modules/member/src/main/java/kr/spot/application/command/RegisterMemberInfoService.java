package kr.spot.application.command;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.MemberRepository;
import kr.spot.presentation.command.dto.request.UpdateMemberNameRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterMemberInfoService {

  private final MemberRepository memberRepository;

  public void updateMemberName(Long memberId, UpdateMemberNameRequest request) {
    int updated = memberRepository.updateNameById(memberId, request.name());
    if (updated == 0) {
      throw new GeneralException(ErrorStatus._FAIL_TO_UPDATE_NAME);
    }
  }
}
