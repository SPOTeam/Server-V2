package kr.spot.ports;

import java.util.List;
import java.util.Map;
import kr.spot.ports.dto.MemberInfoResponse;

public interface GetMemberInfoPort {

  Map<Long, MemberInfoResponse> getMemberInfo(List<Long> memberIds);
}
