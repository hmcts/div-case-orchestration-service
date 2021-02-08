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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Organisation;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CREATED_DATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_CENTRE_SITEID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class SolicitorCreateAndUpdateTest extends IntegrationTest {

    private static final String SOLICITOR_PAYLOAD_CONTEXT_PATH = "/fixtures/solicitor/solicitor-request-data.json";
    private static final String SOLICITOR_REFERENCE = "SolicitorReference";
    private static final String EXISTING_POLICY_REFERENCE_VALUE = "ExistingPolicyReferenceValue";

    @Value("${case.orchestration.solicitor.solicitor-create.context-path}")
    private String solicitorCreatePath;

    @Value("${case.orchestration.solicitor.solicitor-update.context-path}")
    private String solicitorUpdatePath;

    @Test
    public void givenCallbackRequestWhenSolicitorCreateThenReturnUpdatedData() throws Exception {
        Response response = postWithDataAndValidateResponse(getSolicitorCreateUrl(), SOLICITOR_PAYLOAD_CONTEXT_PATH);

        assertEverythingIsFine(response);
    }

    @Test
    public void givenCallbackRequestWhenSolicitorCreateThenReturnUpdatedDataWithValidOrgPolicyReference() throws Exception {
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
    public void givenCallbackRequestWhenSolicitorCreateWithNoSolicitorReferenceThenReturnWithoutOrgPolicyData() throws Exception {
        Response response = postWithoutSolicitorReferenceAndOrganisationPolicyDataAndValidateResponse();

        assertThat(getResponseBody(response), isJson(withoutJsonPath("$.data.PetitionerOrganisationPolicy")));
    }

    @Test
    public void givenCallbackRequestWhenSolicitorUpdateThenReturnUpdatedData() throws Exception {
        Response response = postWithDataAndValidateResponse(getSolicitorUpdateUrl(), SOLICITOR_PAYLOAD_CONTEXT_PATH);

        assertEverythingIsFine(response);
    }

    @Test
    public void givenCallbackRequestWhenSolicitorUpdateThenReturnUpdatedDataWithValidOrgPolicyReference() throws Exception {
        Response response = postWithPetitionerOrganisationPolicyReferenceDataAndValidateResponse();

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
    public void givenCallbackRequestWhenSolicitorUpdateWithNoSolicitorReferenceThenReturnWithOrgPolicyReferenceUnchanged() throws Exception {
        Response response = postWithPetitionerOrganisationPolicyDataAndNoSolicitorReferenceAndValidateResponse();

        assertEverythingIsFine(response);
        assertThat(getResponseBody(response),
            isJson(withJsonPath("$.data.PetitionerOrganisationPolicy.OrgPolicyReference", is(EXISTING_POLICY_REFERENCE_VALUE)))
        );
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

    private Map<String, Object> getRequestHeaders() {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, createSolicitorUser().getAuthToken());
        return headers;
    }

    private Response postWithDataAndValidateResponse(String url, String pathToFileWithData) throws Exception {
        String requestBody = getJsonFromResourceFile(pathToFileWithData, JsonNode.class).toString();
        Response response = RestUtil.postToRestService(url, getRequestHeaders(), requestBody);

        assertThat(HttpStatus.OK.value(), is(response.getStatusCode()));

        return response;
    }

    private Response postWithoutSolicitorReferenceAndOrganisationPolicyDataAndValidateResponse() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = buildCcdCallbackRequest();
        ccdCallbackRequest.getCaseDetails().getCaseData().remove(SOLICITOR_REFERENCE_JSON_KEY);
        ccdCallbackRequest.getCaseDetails().getCaseData().remove(PETITIONER_SOLICITOR_ORGANISATION_POLICY);

        String requestPayload = convertObjectToJsonString(ccdCallbackRequest);

        Response response = RestUtil.postToRestService(getSolicitorCreateUrl(), getRequestHeaders(), requestPayload);

        assertThat(HttpStatus.OK.value(), is(response.getStatusCode()));

        return response;
    }

    private Response postWithPetitionerOrganisationPolicyReferenceDataAndValidateResponse() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = buildCcdCallbackRequest();
        ccdCallbackRequest.getCaseDetails().getCaseData().put(PETITIONER_SOLICITOR_ORGANISATION_POLICY,
            buildPetitionerOrganisationPolicyData());

        String requestPayload = convertObjectToJsonString(ccdCallbackRequest);

        Response response = RestUtil.postToRestService(getSolicitorUpdateUrl(), getRequestHeaders(), requestPayload);

        assertThat(HttpStatus.OK.value(), is(response.getStatusCode()));

        return response;
    }

    private Response postWithPetitionerOrganisationPolicyDataAndNoSolicitorReferenceAndValidateResponse() throws Exception {
        CcdCallbackRequest ccdCallbackRequest = buildCcdCallbackRequest();
        ccdCallbackRequest.getCaseDetails().getCaseData().remove(SOLICITOR_REFERENCE_JSON_KEY);
        ccdCallbackRequest.getCaseDetails().getCaseData().put(PETITIONER_SOLICITOR_ORGANISATION_POLICY, buildPetitionerOrganisationPolicyData());

        String requestPayload = convertObjectToJsonString(ccdCallbackRequest);

        Response response = RestUtil.postToRestService(getSolicitorUpdateUrl(), getRequestHeaders(), requestPayload);

        assertThat(HttpStatus.OK.value(), is(response.getStatusCode()));

        return response;
    }

    private OrganisationPolicy buildPetitionerOrganisationPolicyData() {
        return OrganisationPolicy.builder()
            .orgPolicyReference(EXISTING_POLICY_REFERENCE_VALUE)
            .organisation(Organisation.builder().build())
            .build();
    }

    private String getResponseBody(Response cosResponse) {
        return cosResponse.getBody().asString();
    }

    private CcdCallbackRequest buildCcdCallbackRequest() throws java.io.IOException {
        return getJsonFromResourceFile(SOLICITOR_PAYLOAD_CONTEXT_PATH, CcdCallbackRequest.class);
    }
}
