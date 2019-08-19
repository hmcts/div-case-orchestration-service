package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class DeleteDraftITest extends MockedFunctionalTest {
    private static final String API_URL = "/draftsapi/version/1";
    private static final String CMS_CONTEXT_PATH = "/casemaintenance/version/1/drafts";

    private static final String USER_TOKEN = "Some JWT Token";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenJWTTokenIsNull_whenDeleteDraft_thenReturnBadRequest()
            throws Exception {
        webClient.perform(delete(API_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenThereIsAConnectionError_whenDeleteDraft_thenReturnBadGateway()
            throws Exception {
        final String errorMessage = "some error message";

        stubCmsServerEndpoint(HttpStatus.BAD_GATEWAY, errorMessage);

        webClient.perform(delete(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(content().string(containsString(errorMessage)));
    }


    @Test
    public void givenEverythingWorksAsExpected_whenCmsCalled_thenDeleteDraft()
            throws Exception {


        stubCmsServerEndpoint(HttpStatus.OK, "");

        webClient.perform(delete(API_URL)
                .header(AUTHORIZATION, USER_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));
    }

    private void stubCmsServerEndpoint(HttpStatus status, String body) {
        maintenanceServiceServer.stubFor(WireMock.delete(CMS_CONTEXT_PATH)
                .willReturn(aResponse()
                        .withStatus(status.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                        .withBody(body)));
    }
}