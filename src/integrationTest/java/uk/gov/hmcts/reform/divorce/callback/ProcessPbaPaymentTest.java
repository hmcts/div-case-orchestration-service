package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.divorce.callback.SolicitorCreateAndUpdateTest.postWithDataAndValidateResponse;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PBA_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.PayByAccountTestUtil.setPbaToggleTo;

public class ProcessPbaPaymentTest extends IntegrationTest {

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/solicitor/";

    @Value("${case.orchestration.solicitor.process-pba-payment.context-path}")
    private String contextPath;

    @Test
    public void givenCallbackRequest_whenProcessPbaPaymentAndPbaToggleIsOn_thenReturnDataWithNoErrors() throws Exception {
        setPbaToggleTo(true);
        Response response = postWithDataAndValidateResponse(
                serverUrl + contextPath,
                PAYLOAD_CONTEXT_PATH + "solicitor-request-data-new-pba-field.json",
                createCaseWorkerUser().getAuthToken()
        );

        Map<String, Object> responseData = response.getBody().path(DATA);

        // There will be an error if PBA payment is unsuccessful
        assertNotNull(responseData.get(PBA_NUMBERS));
        assertNoPetitionOnDocumentGeneratedList((List)responseData.get(D8DOCUMENTS_GENERATED));
    }

    @Test
    public void givenCallbackRequest_whenProcessPbaPaymentAndPbaToggleIsOff_thenReturnDataWithNoErrors() throws Exception {
        setPbaToggleTo(false);
        Response response = postWithDataAndValidateResponse(
            serverUrl + contextPath,
            PAYLOAD_CONTEXT_PATH + "solicitor-request-data.json",
            createCaseWorkerUser().getAuthToken()
        );

        Map<String, Object> responseData = response.getBody().path(DATA);

        // There will be an error if PBA payment is unsuccessful
        assertNotNull(responseData.get(SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY));
        assertNoPetitionOnDocumentGeneratedList((List)responseData.get(D8DOCUMENTS_GENERATED));
    }

    private static void assertNoPetitionOnDocumentGeneratedList(List<CollectionMember<Document>> documents) {
        assertEquals(0, documents.stream().filter(ProcessPbaPaymentTest::isPetition).count());
    }

    private static boolean isPetition(CollectionMember<Document> item) {
        return item.getValue().getDocumentType().equalsIgnoreCase(DOCUMENT_TYPE_PETITION);
    }
}
