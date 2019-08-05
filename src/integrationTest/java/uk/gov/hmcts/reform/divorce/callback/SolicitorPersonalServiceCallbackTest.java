package uk.gov.hmcts.reform.divorce.callback;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;

public class SolicitorPersonalServiceCallbackTest extends CcdSubmissionSupport {

    private static final String SUBMIT_COMPLETE_SERVICE_CENTRE_CASE = "submit-complete-service-centre-case.json";
    private static final String PAYMENT_MADE = "payment-made.json";
    private static final String PAYMENT_EVENT_ID = "paymentMade";
    private static final String ISSUE_EVENT_ID = "issueFromSubmitted";
    private static final String DOC_TYPE_PERSONAL_SERVICE = "solicitorPersonalService";
    private static final String PERSONAL_SERVICE_FILE_NAME_FORMAT = "solicitor-personal-service-%s.pdf";

    @Autowired
    private CosApiClient cosApiClient;

    @Test
    public void testSolicitorPersonalServiceCallbackGeneratesPersonalServicePack() {
        //given
        final UserDetails petitionerUserDetails = createCitizenUser();
        CaseDetails caseDetails = submitCase(SUBMIT_COMPLETE_SERVICE_CENTRE_CASE, petitionerUserDetails);

        String caseId = caseDetails.getId().toString();
        updateCase(caseId, PAYMENT_MADE, PAYMENT_EVENT_ID);
        caseDetails = updateCase(caseId, null, ISSUE_EVENT_ID);

        //when
        CcdCallbackRequest callbackRequest = CcdCallbackRequest.builder().caseDetails(
                uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails.builder()
                        .caseId(caseId)
                        .caseData(caseDetails.getData())
                        .build())
                .build();

        CcdCallbackResponse callbackResponse = cosApiClient.processPersonalServicePack(createCaseWorkerUser().getAuthToken(), callbackRequest);

        //then
        CaseDetails responseCaseDetails = CaseDetails.builder()
                .id(Long.valueOf(caseId))
                .data(callbackResponse.getData())
                .build();
        assertGeneratedDocumentsExists(responseCaseDetails, DOC_TYPE_PERSONAL_SERVICE, PERSONAL_SERVICE_FILE_NAME_FORMAT);
    }
}
