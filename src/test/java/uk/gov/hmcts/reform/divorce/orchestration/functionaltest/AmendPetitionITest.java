package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.CourtsMatcher;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.AMEND_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class AmendPetitionITest extends MockedFunctionalTest {
    private static final String CASE_ID = "1234567890";
    private static final String API_URL = "/amend-petition";
    private static final String USER_TOKEN = "Token";
    private static final String PREVIOUS_ID = "Test.Id";
    private static final String CMS_AMEND_PETITION_CONTEXT_PATH = "/casemaintenance/version/1/amended-petition-draft";
    private static final String CMS_UPDATE_CONTEXT_PATH = String.format(
        "/casemaintenance/version/1/updateCase/%s/%s",
        CASE_ID,
        AMEND_PETITION
    );

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenJWTTokenIsNull_whenSaveDraft_thenReturnBadRequest()
        throws Exception {
        webClient.perform(put(API_URL + "/" + CASE_ID)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenAmendPetitionDraftFails_thenReturnAmendPetitionErrorMessage()
        throws Exception {
        final String errorMessage = "amendPetition: An error occurred";

        stubCmsAmendPetitionDraftEndpoint(HttpStatus.BAD_GATEWAY, errorMessage);
        stubCmsUpdateCaseEndpoint(HttpStatus.OK, "{}");

        ResultActions response = webClient.perform(put(API_URL + "/" + CASE_ID)
            .header(AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(content().string(containsString(errorMessage)));
    }

    @Test
    public void givenUpdateCaseFails_thenReturnBadGateway()
        throws Exception {
        final String errorMessage = "error message";

        Map<String, Object> draftData = new HashMap<>();
        draftData.put(PREVIOUS_CASE_ID_JSON_KEY, PREVIOUS_ID);

        String content = convertObjectToJsonString(draftData);

        stubCmsAmendPetitionDraftEndpoint(HttpStatus.OK, content);
        stubCmsUpdateCaseEndpoint(HttpStatus.BAD_GATEWAY, errorMessage);

        ResultActions response = webClient.perform(put(API_URL + "/" + CASE_ID)
            .header(AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isBadGateway()).andExpect(content().string(containsString(errorMessage)));
    }

    @Test
    public void givenNoCaseExists_thenReturnCaseNotFound()
        throws Exception {
        final String errorMessage = "amendPetition: Case not found - please check the case ID: " + CASE_ID;

        stubCmsAmendPetitionDraftEndpoint(HttpStatus.NOT_FOUND, errorMessage);
        stubCmsUpdateCaseEndpoint(HttpStatus.NOT_FOUND, errorMessage);

        ResultActions response = webClient.perform(put(API_URL + "/" + CASE_ID)
            .header(AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(content().string(containsString(errorMessage)));
    }

    @Test
    public void givenNoCaseExists_thenReturnMultipleCasesFoundMessage()
        throws Exception {
        final String errorMessage = "amendPetition: Multiple cases found";

        stubCmsAmendPetitionDraftEndpoint(HttpStatus.MULTIPLE_CHOICES, errorMessage);
        stubCmsUpdateCaseEndpoint(HttpStatus.OK, "{}");

        ResultActions response = webClient.perform(put(API_URL + "/" + CASE_ID)
            .header(AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(content().string(containsString(errorMessage)));
    }

    @Test
    public void givenNoCaseExists_thenReturnUnauthorisedMessage()
        throws Exception {
        final String errorMessage = "amendPetition: Unauthorized (invalid user or case) - caseID: " + CASE_ID;

        stubCmsAmendPetitionDraftEndpoint(HttpStatus.UNAUTHORIZED, errorMessage);
        stubCmsUpdateCaseEndpoint(HttpStatus.OK, "{}");

        ResultActions response = webClient.perform(put(API_URL + "/" + CASE_ID)
            .header(AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(content().string(containsString(errorMessage)));
    }

    @Test
    public void givenEverythingWorksAsExpected_whenCmsCalled_thenSaveDraft()
        throws Exception {

        Map<String, Object> draftData = new HashMap<>();
        draftData.put(PREVIOUS_CASE_ID_JSON_KEY, PREVIOUS_ID);

        stubCmsAmendPetitionDraftEndpoint(HttpStatus.OK, convertObjectToJsonString(draftData));
        stubCmsUpdateCaseEndpoint(HttpStatus.OK, "{}");

        webClient.perform(put(API_URL + "/" + CASE_ID)
            .header(AUTHORIZATION, USER_TOKEN)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(hasJsonPath(PREVIOUS_CASE_ID_JSON_KEY, is(PREVIOUS_ID))))
            .andExpect(content().string(hasJsonPath("court", CourtsMatcher.isExpectedCourtsList())));
    }

    private void stubCmsAmendPetitionDraftEndpoint(HttpStatus status, String body) {
        maintenanceServiceServer.stubFor(WireMock.put(CMS_AMEND_PETITION_CONTEXT_PATH)
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(body)));
    }

    private void stubCmsUpdateCaseEndpoint(HttpStatus status, String body) {
        maintenanceServiceServer.stubFor(WireMock.post(CMS_UPDATE_CONTEXT_PATH)
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(body)));
    }
}

