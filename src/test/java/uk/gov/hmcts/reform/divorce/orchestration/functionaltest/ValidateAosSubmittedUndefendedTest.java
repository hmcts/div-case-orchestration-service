package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Before;
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
import uk.gov.hmcts.reform.divorce.orchestration.OrchestrationServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.TestConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CreateEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DN_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COMPLETED_AOS_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UNREASONABLE_BEHAVIOUR;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = OrchestrationServiceApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ValidateAosSubmittedUndefendedTest {
    private static final String API_URL = "/validate-aos-submitted-undefended";

    private Map<String, Object> caseData;
    private CaseDetails caseDetails;
    private CreateEvent createEvent;

    @Before
    public void setup() {
        caseData = new HashMap<>();
        caseData.put(CASE_ID_JSON_KEY, TestConstants.TEST_CASE_ID);
    }

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenAdulteryCaseAndAosSubmittedUndefendedEvent_whenValidateAosSubmittedUndefended_thenReturnError() throws Exception {
        caseData.put(D_8_REASON_FOR_DIVORCE, ADULTERY);
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);

        caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .caseId(TestConstants.TEST_CASE_ID)
            .state(TestConstants.TEST_STATE)
            .build();

        createEvent = CreateEvent.builder()
            .caseDetails(caseDetails)
            .eventId(AWAITING_DN_AOS_EVENT_ID)
            .build();

        List<String> expectedData = new ArrayList<String>();
        expectedData.add(String.format("%s event should be used for AOS submission in this case",
            COMPLETED_AOS_EVENT_ID));

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .errors(expectedData)
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(createEvent))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }

    @Test
    public void givenCaseData_whenSolicitorCreate_thenReturnEastMidlandsCourtAllocation() throws Exception {
        caseData.put(D_8_REASON_FOR_DIVORCE, UNREASONABLE_BEHAVIOUR);
        caseData.put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);

        caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .caseId(TestConstants.TEST_CASE_ID)
            .state(TestConstants.TEST_STATE)
            .build();

        createEvent = CreateEvent.builder()
            .caseDetails(caseDetails)
            .eventId(AWAITING_DN_AOS_EVENT_ID)
            .build();

        CcdCallbackResponse expected = CcdCallbackResponse.builder()
            .data(caseData)
            .build();

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(createEvent))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(convertObjectToJsonString(expected)));
    }
}
