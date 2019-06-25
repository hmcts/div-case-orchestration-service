package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MAKE_CASE_ELIGIBLE_FOR_DA_PETITIONER_EVENT_ID;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MakeCaseEligibleForDATest {

    private static final String TEST_CASE_ID = "testCaseId";
    private static final String API_URL = "/make-case-eligible-for-da/" + TEST_CASE_ID;

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule maintenanceServiceServer = new WireMockClassRule(4010);

    @Test
    public void testEndpointReturnsSuccessCode() throws Exception {
        String cmsEndpoint = format("/casemaintenance/version/1/updateCase/%s/%s", TEST_CASE_ID, MAKE_CASE_ELIGIBLE_FOR_DA_PETITIONER_EVENT_ID);
        stubFor(WireMock.post(urlEqualTo(cmsEndpoint)).willReturn(aResponse().withStatus(200)));

        webClient.perform(post(API_URL).accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(postRequestedFor(urlEqualTo(cmsEndpoint)).withHeader("Content-Type", equalTo(APPLICATION_JSON_VALUE)));
    }

    @Test
    public void testEndpointReturnsErrorMessage() throws Exception {
        String cmsEndpoint = format("/casemaintenance/version/1/updateCase/%s/%s", TEST_CASE_ID, MAKE_CASE_ELIGIBLE_FOR_DA_PETITIONER_EVENT_ID);
        stubFor(WireMock.post(urlEqualTo(cmsEndpoint)).willReturn(aResponse().withStatus(500)));

        webClient.perform(post(API_URL).accept(APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string(is(notNullValue())));

        verify(postRequestedFor(urlEqualTo(cmsEndpoint)).withHeader("Content-Type", equalTo(APPLICATION_JSON_VALUE)));
    }

}