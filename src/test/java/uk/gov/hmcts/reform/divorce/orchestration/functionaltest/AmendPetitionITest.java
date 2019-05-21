package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PREVIOUS_CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class AmendPetitionITest {
    private static final String CASE_ID = "1234567890";
    private static final String API_URL = "/amend-petition";
    private static final String USER_TOKEN = "Token";
    private static final String PREVIOUS_ID = "Test.Id";
    private static final String CMS_AMEND_PETITION_CONTEXT_PATH = "/casemaintenance/version/1/amended-petition-draft";
    private static final String CMS_UPDATE_CONTEXT_PATH = String.format(
        "/casemaintenance/version/1/updateCase/%s/%s",
        CASE_ID,
        AMEND_PETITION_EVENT
    );

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule cmsServiceServer = new WireMockClassRule(4010);

    @Test
    public void givenJWTTokenIsNull_whenSaveDraft_thenReturnBadRequest()
            throws Exception {
        webClient.perform(put(API_URL + "/" + CASE_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenAmendPetitionDraftFails_thenReturnBadGateway()
            throws Exception {
        final String errorMessage = "{\"error\":\"error message\"}";

        stubCmsAmendPetitionDraftEndpoint(HttpStatus.BAD_GATEWAY, errorMessage);
        stubCmsUpdateCaseEndpoint(HttpStatus.OK, "");

        webClient.perform(put(API_URL + "/" + CASE_ID)
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(content().string(containsString(errorMessage)));
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

        webClient.perform(put(API_URL + "/" + CASE_ID)
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(content().string(containsString(errorMessage)));
    }

    @Test
    public void givenNoCaseExists_thenReturnNotFound()
            throws Exception {
        final String errorMessage = "error message";

        stubCmsAmendPetitionDraftEndpoint(HttpStatus.NOT_FOUND, errorMessage);
        stubCmsUpdateCaseEndpoint(HttpStatus.NOT_FOUND, errorMessage);

        webClient.perform(put(API_URL + "/" + CASE_ID)
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(errorMessage)));
    }

    @Test
    public void givenEverythingWorksAsExpected_whenCmsCalled_thenSaveDraft()
            throws Exception {

        Map<String, Object> draftData = new HashMap<>();
        draftData.put(PREVIOUS_CASE_ID_JSON_KEY, PREVIOUS_ID);

        String content = convertObjectToJsonString(draftData);

        stubCmsAmendPetitionDraftEndpoint(HttpStatus.OK, content);
        stubCmsUpdateCaseEndpoint(HttpStatus.OK, "");

        webClient.perform(put(API_URL + "/" + CASE_ID)
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(content));
    }

    private void stubCmsAmendPetitionDraftEndpoint(HttpStatus status, String body) {
        cmsServiceServer.stubFor(WireMock.put(CMS_AMEND_PETITION_CONTEXT_PATH)
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(body)));
    }

    private void stubCmsUpdateCaseEndpoint(HttpStatus status, String body) {
        cmsServiceServer.stubFor(WireMock.post(CMS_UPDATE_CONTEXT_PATH)
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(body)));
    }
}

