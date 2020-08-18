package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.servicejourney;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.IdamTestSupport;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.ReceivedServiceAddedDateTask;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class ReceivedServiceAddedDateTest extends IdamTestSupport {

    private static final String API_URL = "/received-service-added-date";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenCaseData_whenCalledEndpoint_thenExpectReceivedServiceAddedDateInResponde() throws Exception {
        CcdCallbackRequest input = new CcdCallbackRequest(
            AUTH_TOKEN,
            "event",
            CaseDetails.builder().caseData(new HashMap<>()).caseId(TEST_CASE_ID).build()
        );

        Map<String, Object> expectedCaseData = ImmutableMap.of(
            ReceivedServiceAddedDateTask.RECEIVED_SERVICE_ADDED_DATE, DateUtils.formatDateFromLocalDate(LocalDate.now())
        );

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(convertObjectToJsonString(expectedCaseData))));
    }
}
