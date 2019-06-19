package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.divorce.callback.SolicitorCreateAndUpdateTest.postWithDataAndValidateResponse;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY;

public class ProcessPbaPaymentTest extends IntegrationTest {

    private static final String DATA_KEY = "data";
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/solicitor/";

    @Value("${case.orchestration.solicitor.process-pba-payment.context-path}")
    private String contextPath;

    @Test
    public void givenCallbackRequest_whenProcessPbaPayment_thenReturnDataWithNoErrors() throws Exception {
        Response response = postWithDataAndValidateResponse(
                serverUrl + contextPath,
                PAYLOAD_CONTEXT_PATH + "solicitor-request-data.json",
                createCaseWorkerUser().getAuthToken()
        );

        Map<String, Object> responseData = response.getBody().path(DATA_KEY);

        // There will be an error if PBA payment is unsuccessful
        assertNotNull(responseData.get(SOLICITOR_FEE_ACCOUNT_NUMBER_JSON_KEY));
    }

}
