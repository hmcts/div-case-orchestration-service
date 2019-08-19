package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ResourceLoader.loadResourceAsString;

@RunWith(SpringRunner.class)
public class ValidateBulkCaseListingDataITest extends IdamTestSupport {

    private static final String API_URL = "/bulk/validate/listing";

    private static final String REQUEST_JSON_PATH = "jsonExamples/payloads/bulkCaseCcdCallbackRequest.json";

    private static final String TEST_AUTH_TOKEN = "testAuthToken";

    private static final String ERROR_MESSAGE = "Court hearing date is in the past";

    @Autowired
    private MockMvc webClient;

    @MockBean
    private Clock clock;

    @Before
    public void setup() {
        when(clock.getZone()).thenReturn(UTC);
    }

    @Test
    public void givenCallbackRequestWithFutureDateBulkCaseData_thenReturnCallbackResponse() throws Exception {
        // Mock current date to be in the past compared to the request json
        LocalDateTime today = LocalDateTime.parse("1999-01-01T10:20:55.000");
        when(clock.instant()).thenReturn(today.toInstant(ZoneOffset.UTC));

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .header(AUTHORIZATION, TEST_AUTH_TOKEN)
                .content(loadResourceAsString(REQUEST_JSON_PATH))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", nullValue()));
    }

    @Test
    public void givenCallbackRequestWithPastDateBulkCaseData_thenReturnCallbackResponseWithErrors() throws Exception {
        // Mock current date to be in the past compared to the request json
        LocalDateTime today = LocalDateTime.parse("2001-01-01T10:20:55.000");
        when(clock.instant()).thenReturn(today.toInstant(ZoneOffset.UTC));

        webClient.perform(MockMvcRequestBuilders.post(API_URL)
                .header(AUTHORIZATION, TEST_AUTH_TOKEN)
                .content(loadResourceAsString(REQUEST_JSON_PATH))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", contains(ERROR_MESSAGE)));
    }
}