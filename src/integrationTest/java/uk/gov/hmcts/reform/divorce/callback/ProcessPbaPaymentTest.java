package uk.gov.hmcts.reform.divorce.callback;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ProcessPbaPaymentTask;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmcts.reform.divorce.callback.SolicitorCreateAndUpdateTest.postWithDataAndValidateResponse;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PBA_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;

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

        assertThat(responseData.get(PBA_NUMBERS), notNullValue());
        assertThat(responseData.get(ProcessPbaPaymentTask.PAYMENT_STATUS), nullValue());
        assertNoPetitionOnDocumentGeneratedList((List) responseData.get(D8DOCUMENTS_GENERATED));
    }

    private static void assertNoPetitionOnDocumentGeneratedList(List<CollectionMember<Document>> documents) {
        List<CollectionMember<Document>> numberOfDocuments = documents.stream().filter(ProcessPbaPaymentTest::isPetition).collect(toList());
        assertThat(numberOfDocuments, hasSize(0));
    }

    private static boolean isPetition(CollectionMember<Document> item) {
        return item.getValue().getDocumentType().equalsIgnoreCase(DOCUMENT_TYPE_PETITION);
    }
}
