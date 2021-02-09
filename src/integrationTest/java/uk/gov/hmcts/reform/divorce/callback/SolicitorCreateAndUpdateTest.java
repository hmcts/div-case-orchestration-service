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
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CREATED_DATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class SolicitorCreateAndUpdateTest extends IntegrationTest {

    private static final String SOLICITOR_PAYLOAD_CONTEXT_PATH = "/fixtures/solicitor/solicitor-request-data.json";
    private static final String SOLICITOR_REFERENCE = "SolicitorReference";

    @Value("${case.orchestration.solicitor.solicitor-create.context-path}")
    private String solicitorCreatePath;

    @Value("${case.orchestration.solicitor.solicitor-update.context-path}")
    private String solicitorUpdatePath;

    @Test
    public void givenCallbackRequest_whenSolicitorCreate_thenReturnUpdatedData() throws Exception {
        Response response = postWithDataAndValidateResponse(getSolicitorCreateUrl(), SOLICITOR_PAYLOAD_CONTEXT_PATH);

        assertEverythingIsFine(response);
    }

    @Test
    public void givenCallbackRequest_whenSolicitorCreate_thenReturnUpdatedDataWithValidOrgPolicyReference() throws Exception {
        Response response = postWithDataAndValidateResponse(getSolicitorCreateUrl(), SOLICITOR_PAYLOAD_CONTEXT_PATH);

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
    public void givenCallbackRequest_whenSolicitorCreateWithNoSolicitorReference_thenReturnWithoutOrgPolicyData() throws Exception {
        Response response = postWithoutSolicitorReferenceDataAndValidateResponse();

        assertThat(getResponseBody(response),
            isJson(withoutJsonPath("$.data.PetitionerOrganisationPolicy"))
        );
    }

    @Test
    public void givenCallbackRequest_whenSolicitorUpdate_thenReturnUpdatedData() throws Exception {
        Response response = postWithDataAndValidateResponse(getSolicitorUpdateUrl(), SOLICITOR_PAYLOAD_CONTEXT_PATH);

        assertEverythingIsFine(response);
    }

    private void assertEverythingIsFine(Response response) {
        Map<String, Object> responseData = response.getBody().path(DATA);

        assertThat(responseData.get(CREATED_DATE_JSON_KEY), is(notNullValue()));
        assertThat(responseData.get(DIVORCE_UNIT_JSON_KEY), is(notNullValue()));
        assertThat(responseData.get(DIVORCE_CENTRE_SITEID_JSON_KEY), is(notNullValue()));
    }

    private String getSolicitorCreateUrl() {
        return serverUrl + solicitorCreatePath;
    }

    private String getSolicitorUpdateUrl() {
        return serverUrl + solicitorUpdatePath;
    }

    private Response postWithDataAndValidateResponse(String url, String pathToFileWithData) throws Exception {
        String requestBody = getJsonFromResourceFile(pathToFileWithData, JsonNode.class).toString();
        Response response = RestUtil.postToRestService(url, getRequestHeaders(), requestBody);

        assertThat(HttpStatus.OK.value(), is(response.getStatusCode()));

        return response;
    }

    private Map<String, Object> getRequestHeaders() {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, createSolicitorUser().getAuthToken());
        return headers;
    }

    private Response postWithoutSolicitorReferenceDataAndValidateResponse() throws java.io.IOException {
        CcdCallbackRequest requestData = getJsonFromResourceFile(SOLICITOR_PAYLOAD_CONTEXT_PATH, CcdCallbackRequest.class);
        requestData.getCaseDetails().getCaseData().remove(SOLICITOR_REFERENCE_JSON_KEY);
        String requestPayload = convertObjectToJsonString(requestData);

        Response response = RestUtil.postToRestService(getSolicitorCreateUrl(), getRequestHeaders(), requestPayload);

        assertThat(HttpStatus.OK.value(), is(response.getStatusCode()));

        return response;
    }

    private String getResponseBody(Response cosResponse) {
        return cosResponse.getBody().asString();
    }
}
