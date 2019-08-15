package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class SolicitorPersonalServiceCallbackTest extends CcdSubmissionSupport {

    private static final String ISSUED_SOLICITOR_PETITION_JSON = "solicitor-petition.json";
    private static final String SOLICITOR_SUBMIT_PERSONAL_SERVICE = "solicitor-submit-personal-service.json";
    private static final String ISSUE_EVENT_ID = "issueFromSubmitted";
    private static final String DOC_TYPE_PERSONAL_SERVICE = "personalService";
    private static final String PERSONAL_SERVICE_FILE_NAME_FORMAT = "solicitor-personal-service-%s.pdf";
    private static final String SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT = "solicitorStatementOfTruthPaySubmit";
    private static final String PAYMENT_MADE_FROM_AWAITING_PAY_DECISION = "paymentMadeFromSolicitorAwaitingPayConfirm";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    @Ignore //until div-ccd-definitions PR-210 is in master/AAT
    public void testSolicitorPersonalServiceCallbackGeneratesPersonalServicePack() {
        //given
        final UserDetails solicitorUser = createSolicitorUser();
        CaseDetails caseDetails = submitSolicitorCase(ISSUED_SOLICITOR_PETITION_JSON, solicitorUser);

        String caseId = caseDetails.getId().toString();
        updateCase(caseId, SOLICITOR_SUBMIT_PERSONAL_SERVICE, SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT, solicitorUser);
        updateCase(caseId, null, PAYMENT_MADE_FROM_AWAITING_PAY_DECISION);
        caseDetails = updateCase(caseId, null, ISSUE_EVENT_ID);

        //when
        CcdCallbackRequest callbackRequest = CcdCallbackRequest.builder().caseDetails(
                uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails.builder()
                        .caseId(caseId)
                        .caseData(caseDetails.getData())
                        .build())
                .build();

        CcdCallbackResponse callbackResponse = cosApiClient.processPersonalServicePack(
                createSolicitorUser().getAuthToken(),
                callbackRequest
        );

        //then
        assertThat(callbackResponse.getErrors(), is(nullValue()));
        CaseDetails responseCaseDetails = CaseDetails.builder()
                .id(Long.valueOf(caseId))
                .data(callbackResponse.getData())
                .build();
        assertGeneratedDocumentsExists(responseCaseDetails, DOC_TYPE_PERSONAL_SERVICE, PERSONAL_SERVICE_FILE_NAME_FORMAT);
    }
}
