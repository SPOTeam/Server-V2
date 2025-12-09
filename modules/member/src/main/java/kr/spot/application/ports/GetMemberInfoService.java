package kr.spot.application.ports;

import java.util.List;
import java.util.Map;
import kr.spot.domain.Member;
import kr.spot.infrastructure.jpa.MemberRepository;
import kr.spot.ports.GetMemberInfoPort;
import kr.spot.ports.dto.MemberInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMemberInfoService implements GetMemberInfoPort {

  private final MemberRepository memberRepository;

  @Override
  public Map<Long, MemberInfoResponse> getMemberInfo(List<Long> memberIds) {
    List<Member> members = memberRepository.findAllById(memberIds);

    return members.stream()
        .collect(
            java.util.stream.Collectors.toMap(
                Member::getId,
                member -> new MemberInfoResponse(
                    member.getName(),
                    member.getProfileImageUrl()
                )
            )
        );
  }
}
