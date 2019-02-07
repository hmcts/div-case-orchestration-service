package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

public interface CourtAllocator {

    String selectCourtForGivenDivorceFact(String fact);

}