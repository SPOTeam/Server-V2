package kr.spot.common;

import java.util.List;
import java.util.stream.LongStream;
import kr.spot.domain.Study;
import kr.spot.domain.vo.Fee;

public class StudyFixture {
    public static Long ID = 1L;
    public static Long LEADER_ID = 2L;
    public static String NAME = "Study Name";
    public static String DESCRIPTION = "Study Description";
    public static String IMAGE_URL = "http://example.com/image.png";
    public static int MAX_MEMBERS = 10;
    public static boolean HAS_FEE = true;
    public static int FEE_AMOUNT = 1_000;

    public static Study study() {
        return Study.of(ID, LEADER_ID, NAME, MAX_MEMBERS, Fee.of(HAS_FEE, FEE_AMOUNT), IMAGE_URL, DESCRIPTION);
    }

    public static List<Study> createStudies(int count) {
        return LongStream.range(0, count)
            .mapToObj(i -> {
                long studyId = count - i; // 10, 9, 8, ...
                return Study.of(studyId, LEADER_ID, NAME + " " + studyId, MAX_MEMBERS, Fee.of(HAS_FEE, FEE_AMOUNT), IMAGE_URL, DESCRIPTION);
            })
            .toList();
    }
}
