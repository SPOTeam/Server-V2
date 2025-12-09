package kr.spot.infrastructure.jpa.associations;

import kr.spot.domain.associations.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

}
