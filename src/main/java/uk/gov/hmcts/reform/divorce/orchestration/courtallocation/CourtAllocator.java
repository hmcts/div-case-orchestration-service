package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import java.util.Optional;

public interface CourtAllocator {

    String selectCourtForGivenDivorceReason(Optional<String> reasonForDivorce);

}