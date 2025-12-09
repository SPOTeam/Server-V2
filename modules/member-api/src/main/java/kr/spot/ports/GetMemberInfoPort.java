package kr.spot.ports;

import kr.spot.ports.dto.MemberInfoResponse;

public interface GetMemberInfoPort {

  MemberInfoResponse getMemberInfo(long memberId);
}
