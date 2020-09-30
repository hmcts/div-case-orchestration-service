package uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskContextConstants {

    public static final String CCD_CASE_DATA = "ccdCaseData";
    public static final String DN_COURT_DETAILS = "dnCourtDetails";
    public static final String BULK_LINK_CASE_ID = "bulkLinkCaseId";
    public static final String DIVORCE_PARTY = "divorceParty";
    public static final String CASE_ID_KEY = "caseId";
    public static final String CASE_STATE_KEY = "caseState";
    public static final String COURT_KEY = "court";

}