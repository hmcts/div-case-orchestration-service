package uk.gov.hmcts.reform.divorce.maintenance;

import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ProcessPbaPaymentTask;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PBA_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;

public class MovedProcessPbaPaymentTest extends IntegrationTest {

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

    @Test
    public void givenCallbackRequestWhenProcessPbaPaymentThenReturnDataWithErrors() throws Exception {
        Response response = postInvalidDataAndReturnResponse(
            serverUrl + contextPath,
            PAYLOAD_CONTEXT_PATH + "solicitor-request-invalid-data.json",
            createCaseWorkerUser().getAuthToken()
        );

        assertThat(response.getStatusCode(),is(HttpStatus.BAD_REQUEST));
    }

    private static void assertNoPetitionOnDocumentGeneratedList(List<CollectionMember<Document>> documents) {
        List<CollectionMember<Document>> numberOfDocuments = documents.stream().filter(MovedProcessPbaPaymentTest::isPetition).collect(toList());
        assertThat(numberOfDocuments, hasSize(0));
    }

    private static boolean isPetition(CollectionMember<Document> item) {
        return item.getValue().getDocumentType().equalsIgnoreCase(DOCUMENT_TYPE_PETITION);
    }

    static Response postWithDataAndValidateResponse(
        String url, String pathToFileWithData, String authToken) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, authToken);

        Response response = RestUtil.postToRestService(url, headers, ResourceLoader.loadJson(pathToFileWithData));

        assertEquals(HttpStatus.OK.value(), response.getStatusCode());

        return response;
    }

    static Response postInvalidDataAndReturnResponse(
        String url, String pathToFileWithData, String authToken) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, authToken);
        return RestUtil.postToRestService(url, headers, ResourceLoader.loadJson(pathToFileWithData));
    }
}