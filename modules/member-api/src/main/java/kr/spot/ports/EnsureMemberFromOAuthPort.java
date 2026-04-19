package kr.spot.ports;

import kr.spot.ports.dto.EnsureResult;

public interface EnsureMemberFromOAuthPort {
    EnsureResult ensure(String provider, String email, String nickname, String imageUrl);
}