package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

public class SetDNGrantedDateITest extends IdamTestSupport {

    private static final String API_URL = "/dn-pronounced-manual";
    private static final String MISSING_JUDGE_REQUEST_JSON_PATH = "jsonExamples/payloads/bulkCaseCcdCallbackRequestNoJudge.json";


    @Autowired
    ThreadPoolTaskExecutor asyncTaskExecutor;

    @Autowired
    private MockMvc webClient;

    @Before
    public void setup() {
        maintenanceServiceServer.resetAll();
    }

    @Test
    public void givenCallbackRequestWithDnPronouncementDateBulkCaseData_thenReturnCallbackResponse() throws Exception {


        // Matching request json
        String courtHearingDate = "2000-01-01";
        String eligibleDate = "2000-02-13";
        String pronouncementJudge = "District Judge";

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(loadResourceAsString("jsonExamples/payloads/bulkCaseCcdCallbackRequest.json"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.DecreeNisiGrantedDate", equalTo(courtHearingDate)))
                .andExpect(jsonPath("$.data.DAEligibleFromDate", equalTo(eligibleDate)))
                .andExpect(jsonPath("$.data.PronouncementJudge", equalTo(pronouncementJudge)))
                .andExpect(jsonPath("$.errors", nullValue()));
    }

    @Test
    public void givenCallbackRequestWithNoJudgeCaseData_thenReturnCallbackResponseWithError() throws Exception {
        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .content(loadResourceAsString(MISSING_JUDGE_REQUEST_JSON_PATH))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", notNullValue()));
    }
}