package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@RunWith(SpringRunner.class)
public class RemoveBulkLinkITest extends IdamTestSupport {

    private static final String API_URL = "/remove-bulk-link";

    private static final String REQUEST_JSON_PATH = "jsonExamples/payloads/genericPetitionerData.json";

    private static final String TEST_AUTH_TOKEN = "testAuthToken";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenRemoveBulkLinkCallback_thenReturnCallbackResponse() throws Exception {

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .header(AUTHORIZATION, TEST_AUTH_TOKEN)
                .content(loadResourceAsString(REQUEST_JSON_PATH))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.data.BulkListingCaseId").doesNotExist())
                .andExpect(jsonPath("$.errors", nullValue()));
    }

}