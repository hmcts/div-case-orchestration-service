package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class EditBulkCaseITest extends MockedFunctionalTest {
    private static final String API_URL = "/bulk/edit/listing?templateId=a&documentType=b&filename=c";
    private static final String ERROR_MESSAGE = "Court hearing date is in the past";

    private Map<String, Object> caseData;

    private CaseDetails caseDetails;

    private CcdCallbackRequest ccdCallbackRequest;

    @Autowired
    private MockMvc webClient;

    @MockBean
    private Clock clock;

    @Before
    public void setup() {
        caseData = new HashMap<>();
        caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .build();
        ccdCallbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(clock.getZone()).thenReturn(UTC);
    }

    @Test
    public void givenCaseWithJudge_whenEditBulkCase_thenGenerateDocument() throws Exception {

        LocalDateTime today = LocalDateTime.parse("1999-01-01T10:20:55.000");
        when(clock.instant()).thenReturn(today.toInstant(ZoneOffset.UTC));

        final String templateId = "a";
        final String documentType = "b";

        caseData.put("hearingDate", "2000-01-01T10:20:55.000");
        caseData.put("PronouncementJudge", "Judge");

        stubDocumentGeneratorService(templateId, singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails), documentType);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(ImmutableMap.of("data", caseData))));
    }

    @Test
    public void givenCallbackRequestWithPastDateBulkCaseData_thenReturnCallbackResponseWithErrors() throws Exception {
        // Mock current date to be in the past compared to the request json
        LocalDateTime today = LocalDateTime.parse("2001-01-01T10:20:55.000");
        when(clock.instant()).thenReturn(today.toInstant(ZoneOffset.UTC));

        caseData.put("hearingDate", "2000-01-01T10:20:55.000");

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors", contains(ERROR_MESSAGE)));
    }

}
