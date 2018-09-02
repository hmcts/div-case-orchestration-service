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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
public class RetrieveDraftITest {
    private static final String API_URL = "/draft";
    private static final String CMS_CONTEXT_PATH = "/casemaintenance/version/1/retrieveCase";

    private static final String USER_TOKEN = "Some JWT Token";
    private static final String CASE_ID = "12345";

    private static final Map<String, Object> CASE_DATA = new HashMap<>();
    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
            .caseData(CASE_DATA)
            .caseId(CASE_ID)
            .build();


    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule cmsServiceServer = new WireMockClassRule(4010);

    @Test
    public void givenJWTTokenIsNull_whenRetrieveDraft_thenReturnBadRequest()
            throws Exception {
        webClient.perform(get(API_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenThereIsAConnectionError_whenRetrieveDraft_thenReturnBadGateway()
            throws Exception {
        final String errorMessage = "some error message";

        stubCmsServerEndpoint(HttpStatus.BAD_GATEWAY, errorMessage);

        webClient.perform(get(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(content().string(containsString(errorMessage)));
    }

    @Test
    public void givenNoDraftInDraftStore_whenRetrieveraft_thenReturnNotFound()
            throws Exception {

        stubCmsServerEndpoint(HttpStatus.OK, convertObjectToJsonString(CASE_DETAILS));

        webClient.perform(get(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    public void givenEverythingWorksAsExpected_whenCmsCalled_thenReturnDraft()
            throws Exception {

        CASE_DATA.put("deaftProperty1", "value1");
        CASE_DATA.put("deaftProperty2", "value2");

        stubCmsServerEndpoint(HttpStatus.OK, convertObjectToJsonString(CASE_DETAILS));

        webClient.perform(get(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(convertObjectToJsonString(CASE_DATA)));
    }

    private void stubCmsServerEndpoint(HttpStatus status, String body)
            throws Exception {
        cmsServiceServer.stubFor(WireMock.get(CMS_CONTEXT_PATH)
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(body)));
    }
}
