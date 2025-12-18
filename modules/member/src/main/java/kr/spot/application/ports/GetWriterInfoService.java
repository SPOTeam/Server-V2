package kr.spot.application.ports;

import kr.spot.domain.Member;
import kr.spot.infrastructure.jpa.MemberRepository;
import kr.spot.ports.GetWriterInfoPort;
import kr.spot.ports.dto.WriterInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWriterInfoService implements GetWriterInfoPort {

  private final MemberRepository memberRepository;

  @Override
  public WriterInfoResponse get(long memberId) {
    Member member = memberRepository.getMemberById(memberId);
    return WriterInfoResponse.of(member.getId(), member.getName(), member.getProfileImageUrl());
  }
}
