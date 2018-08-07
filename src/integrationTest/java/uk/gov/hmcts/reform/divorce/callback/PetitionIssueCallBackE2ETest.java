package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;


public class PetitionIssueCallBackE2ETest extends CcdSubmissionSupport {
    private static final String SUBMIT_COMPLETE_CASE = "submit-complete-case.json";
    private static final String PAYMENT_MADE = "payment-made.json";
    private static final String PAYMENT_EVENT_ID = "paymentMade";
    private static final String ISSUE_EVENT_ID = "issueFromSubmitted";

    @Test
    public void submittingCaseAndIssuePetitionOnCcdShouldGeneratePDF() {
        //submit case
        CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_CASE);
        //make payment
        CaseDetails caseDetailsAfterPayment =
            updateCase(caseDetails.getId().toString(), PAYMENT_MADE, PAYMENT_EVENT_ID);
        //issue
        CaseDetails caseDetailsAfterIssue =
            updateCase(caseDetails.getId().toString(), PAYMENT_MADE, ISSUE_EVENT_ID);

        assertGeneratedDocumentExists(caseDetailsAfterIssue, caseDetails.getId());
    }




}
