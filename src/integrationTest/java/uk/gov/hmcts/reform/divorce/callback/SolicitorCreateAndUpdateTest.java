package uk.gov.hmcts.reform.divorce.callback;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CREATED_DATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.SetSolicitorOrganisationPolicyDetailsTaskTest.SOLICITOR_REFERENCE_MISSING;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class SolicitorCreateAndUpdateTest extends IntegrationTest {

    private static final String PAYLOAD_CONTEXT_PATH = "/fixtures/solicitor/";
    private static final String SOLICITOR_REFERENCE = "SolicitorReference";

    @Value("${case.orchestration.solicitor.solicitor-create.context-path}")
    private String solicitorCreatePath;

    @Value("${case.orchestration.solicitor.solicitor-update.context-path}")
    private String solicitorUpdatePath;

    @Test
    public void givenCallbackRequest_whenSolicitorCreate_thenReturnUpdatedData() throws Exception {
        Response response = postWithDataAndValidateResponse(
            serverUrl + solicitorCreatePath,
            PAYLOAD_CONTEXT_PATH + "solicitor-request-data.json",
            createSolicitorUser().getAuthToken()
        );

        assertEverythingIsFine(response);
    }

    @Test
    public void givenCallbackRequest_whenSolicitorCreate_thenReturnUpdatedDataWithValidOrgPolicyReference() throws Exception {
        Response response = postWithDataAndValidateResponse(
            serverUrl + solicitorCreatePath,
            PAYLOAD_CONTEXT_PATH + "solicitor-request-data.json",
            createSolicitorUser().getAuthToken()
        );

        assertEverythingIsFine(response);
        assertThat(getResponseBody(response),
            isJson(
                allOf(
                    withJsonPath("$.data.D8SolicitorReference", is(SOLICITOR_REFERENCE)),
                    withJsonPath("$.data.PetitionerOrganisationPolicy.OrgPolicyReference", is(SOLICITOR_REFERENCE)))
            )
        );
    }

    @Test
    public void givenCallbackRequest_whenSolicitorCreateWithNoSolicitorReference_thenReturnWithError() throws Exception {
        Response response = postWithInValidDataAndValidateResponse();

        assertThat(getResponseBody(response), isJson(
            allOf(
                withJsonPath("$.errors", hasSize(1)),
                withJsonPath("$.errors[0]", is(SOLICITOR_REFERENCE_MISSING))
            )));
    }

    @Test
    public void givenCallbackRequest_whenSolicitorUpdate_thenReturnUpdatedData() throws Exception {
        Response response = postWithDataAndValidateResponse(
            serverUrl + solicitorUpdatePath,
            PAYLOAD_CONTEXT_PATH + "solicitor-request-data.json",
            createSolicitorUser().getAuthToken()
        );

        assertEverythingIsFine(response);
    }

    private static void assertEverythingIsFine(Response response) {
        Map<String, Object> responseData = response.getBody().path(DATA);

        assertThat(responseData.get(CREATED_DATE_JSON_KEY), is(notNullValue()));
        assertThat(responseData.get(DIVORCE_UNIT_JSON_KEY), is(notNullValue()));
        assertThat(responseData.get(DIVORCE_CENTRE_SITEID_JSON_KEY), is(notNullValue()));
    }

    static Response postWithDataAndValidateResponse(String url, String pathToFileWithData, String authToken) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, authToken);

        String requestBody = getJsonFromResourceFile(pathToFileWithData, JsonNode.class).toString();
        Response response = RestUtil.postToRestService(url, headers, requestBody);

        assertThat(HttpStatus.OK.value(), is(response.getStatusCode()));

        return response;
    }

    private Response postWithInValidDataAndValidateResponse() throws java.io.IOException {
        CcdCallbackRequest jsonFromResourceFile = getJsonFromResourceFile(PAYLOAD_CONTEXT_PATH + "solicitor-request-data.json",
            CcdCallbackRequest.class);
        jsonFromResourceFile.getCaseDetails().getCaseData().remove(SOLICITOR_REFERENCE_JSON_KEY);
        String requestBody = convertObjectToJsonString(jsonFromResourceFile);

        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, createSolicitorUser().getAuthToken());

        Response response = RestUtil.postToRestService(serverUrl + solicitorCreatePath, headers, requestBody);

        assertThat(HttpStatus.OK.value(), is(response.getStatusCode()));

        return response;
    }

    private String getResponseBody(Response cosResponse) {
        return cosResponse.getBody().asString();
    }
}
