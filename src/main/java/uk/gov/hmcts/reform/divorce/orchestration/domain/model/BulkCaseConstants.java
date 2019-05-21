package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BulkCaseConstants {

    public static final String BULK_CASE_TITLE_KEY = "CaseTitle";
    public static final String BULK_CASE_ACCEPTED_LIST_KEY = "CaseAcceptedList";
    public static final String CASE_LIST_KEY = "CaseList";

    public static final String VALUE_KEY = "value";
    public static final String CASE_REFERENCE_FIELD = "CaseReference";
    public static final String CASE_PARTIES_FIELD = "CaseParties";
    public static final String DN_APPROVAL_DATE_FIELD = "DNApprovalDate";
    public static final String COST_ORDER_FIELD = "CostOrder";
    public static final String FAMILY_MAN_REFERENCE_FIELD = "FamilyManReference";
    public static final String BULK_CASE_LIST_KEY = "BulkCases";
    public static final String SEARCH_RESULT_KEY = "SearchResult";

    public static final String BULKCASE_CREATION_ERROR = "BulKCaseCreation_Error";

}
