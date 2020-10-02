package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmcts.reform.divorce.callback.SolicitorCreateAndUpdateTest.postWithDataAndValidateResponse;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY;

public class ProcessPbaPaymentTest extends IntegrationTest {

    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/solicitor/";

    @Value("${case.orchestration.solicitor.process-pba-payment.context-path}")
    private String contextPath;

    @Test
    public void givenCallbackRequestWhenProcessPbaPaymentThenReturnDataWithNoErrors() throws Exception {
        Response response = postWithDataAndValidateResponse(
            serverUrl + contextPath,
            PAYLOAD_CONTEXT_PATH + "solicitor-request-data.json",
            createCaseWorkerUser().getAuthToken()
        );

        Map<String, Object> responseData = response.getBody().path(DATA);
        String state = response.getBody().path(STATE);

        assertThat(responseData.get(SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY), notNullValue());
        assertThat(state, is(CcdStates.SUBMITTED));
        assertNoPetitionOnDocumentGeneratedList((List) responseData.get(D8DOCUMENTS_GENERATED));
    }

    @Test
    public void givenCallbackRequestWhenProcessPbaPaymentThenReturnDataWithErrors() throws Exception {
        Response response = postWithDataAndValidateResponse(
            serverUrl + contextPath,
            PAYLOAD_CONTEXT_PATH + "solicitor-error-request-data.json",
            createCaseWorkerUser().getAuthToken()
        );

        Map<String, Object> responseData = response.getBody().path(DATA);
        List<String> errors = response.getBody().path(ERRORS);
        String state = response.getBody().path(STATE);

        assertThat(responseData, nullValue());
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), is("Statement of truth for solicitor and petitioner needs to be accepted"));
        assertThat(state, nullValue());
    }

    private static void assertNoPetitionOnDocumentGeneratedList(List<CollectionMember<Document>> documents) {
        int numberOfDocuments = (int) documents.stream().filter(ProcessPbaPaymentTest::isPetition).count();
        assertThat(numberOfDocuments, is(0));
    }

    private static boolean isPetition(CollectionMember<Document> item) {
        return item.getValue().getDocumentType().equalsIgnoreCase(DOCUMENT_TYPE_PETITION);
    }
}
