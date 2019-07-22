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
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class SaveDraftITest {
    private static final String API_URL = "/draftsapi/version/1";
    private static final String CMS_CONTEXT_PATH = "/casemaintenance/version/1/drafts?divorceFormat=true";

    private static final String USER_TOKEN = "Some JWT Token";

    private static final Map<String, String> CASE_DATA = new HashMap<>();
    private static final Map<String, Object> CASE_DETAILS = new HashMap<>();
    private static final String EMAIL_CONTEXT_PATH = "https://api.notifications.service.gov.uk";


    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule cmsServiceServer = new WireMockClassRule(4010);

    @ClassRule
    public static WireMockClassRule emailServiceServer = new WireMockClassRule(9999);

    @Test
    public void givenJWTTokenIsNull_whenSaveDraft_thenReturnBadRequest()
            throws Exception {
        webClient.perform(put(API_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenThereIsAConnectionError_whenSaveDraft_thenReturnBadGateway()
            throws Exception {
        final String errorMessage = "some error message";

        stubCmsServerEndpoint(HttpStatus.BAD_GATEWAY, errorMessage);

        CASE_DATA.put("deaftProperty1", "value1");
        CASE_DETAILS.put("case_data", CASE_DATA);

        webClient.perform(put(API_URL)
                .content(convertObjectToJsonString(CASE_DETAILS))
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(content().string(containsString(errorMessage)));
    }

    @Test
    public void givenEverythingWorksAsExpected_whenCmsCalled_thenSaveDraft()
            throws Exception {

        CASE_DATA.put("deaftProperty1", "value1");
        CASE_DETAILS.put("case_data", CASE_DATA);

        stubCmsServerEndpoint(HttpStatus.OK, convertObjectToJsonString(CASE_DETAILS));

        stubEmailServerEndpoint(HttpStatus.OK, "Success");

        webClient.perform(put(API_URL)
                .content(convertObjectToJsonString(CASE_DETAILS))
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));
    }

    private void stubCmsServerEndpoint(HttpStatus status, String body) {
        cmsServiceServer.stubFor(WireMock.put(CMS_CONTEXT_PATH)
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(body)));
    }

    private void stubEmailServerEndpoint(HttpStatus status, String body) {
        emailServiceServer.stubFor(WireMock.put(EMAIL_CONTEXT_PATH)
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(body)));
    }

}

