package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.util.Collections;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AOS_AWAITING_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_DRAFTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.ISSUED_TO_BAILIFF;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class ConfirmSolDNReview extends MockedFunctionalTest {

    private static final String API_URL = "/confirm-sol-dn-review";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenAOSDraftedCandidateState_whenEnpointIsCalled_thenShouldChangeState() throws Exception {

        final Map<String, Object> caseData = Collections.emptyMap();

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .state(AOS_AWAITING_STATE)
                .build();

        CcdCallbackRequest request = CcdCallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.state", is(AOS_DRAFTED)),
                        hasNoJsonPath("$.errors"),
                        hasNoJsonPath("$.warnings")
                )));
    }

    @Test
    public void givenNotAOSDraftedCandidateState_whenEnpointIsCalled_thenShouldNotChangeState() throws Exception {

        final Map<String, Object> caseData = Collections.emptyMap();

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .caseId(TEST_CASE_ID)
                .state(ISSUED_TO_BAILIFF)
                .build();

        CcdCallbackRequest request = CcdCallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();

        webClient.perform(post(API_URL)
                .content(convertObjectToJsonString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        isJson(),
                        hasJsonPath("$.state", is(ISSUED_TO_BAILIFF)),
                        hasNoJsonPath("$.errors"),
                        hasNoJsonPath("$.warnings")
                )));
    }
}