package kr.spot.application.query;

import kr.spot.domain.Member;
import kr.spot.infrastructure.jpa.MemberRepository;
import kr.spot.presentation.query.dto.response.GetMemberNameResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMemberInfoService {

  private final MemberRepository memberRepository;

  public GetMemberNameResponse getMemberName(Long memberId) {
    Member member = memberRepository.getMemberById(memberId);
    return GetMemberNameResponse.from(member.getName());
  }
}
