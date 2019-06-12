package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class UpdateBulkCaseDnPronouncementDateITest extends IdamTestSupport {

    private static final String API_URL = "/bulk/pronounce/submit";

    private static final String REQUEST_JSON_PATH = "jsonExamples/payloads/bulkCaseCcdCallbackRequest.json";

    private static final String TEST_AUTH_TOKEN = "testAuthToken";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenCallbackRequestWithCourtHearingBulkCaseData_thenReturnCallbackResponse() throws Exception {
        // Matching request json
        String courtHearingDate = "2000-01-01";
        String eligibleDate = "2000-02-13";
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .header(AUTHORIZATION, TEST_AUTH_TOKEN)
                .content(loadResourceAsString(REQUEST_JSON_PATH))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.DecreeNisiGrantedDate", equalTo(courtHearingDate)))
                .andExpect(jsonPath("$.data.DAEligibleFromDate", equalTo(eligibleDate)))
                .andExpect(jsonPath("$.errors", nullValue()));
    }
}