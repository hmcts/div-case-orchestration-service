package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_COSTS_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_TYPE_COSTS_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.JUDGE_WHO_PAYS_COSTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class JudgeCostsDecisionTest extends IdamTestSupport {

    private static final String API_URL = "/judge-costs-decision";

    @Autowired
    private MockMvc webClient;

    private CcdCallbackRequest buildRequest(Map<String, Object> caseData) {

        return new CcdCallbackRequest(
            AUTH_TOKEN,
            "judgeCostOrderDecision",
            CaseDetails.builder().caseData(caseData).caseId(TEST_CASE_ID).build()
        );
    }

    @Test
    public void givenJudgeMakesDecision_whenCalledEndpoint_thenJudgeCostsDecisionIsPopulated() throws Exception {
        Map<String, Object> caseData = new HashMap<>();

        CcdCallbackRequest input = buildRequest(caseData);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(input))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.data.JudgeCostsDecision", is(YES_VALUE)),
                    hasNoJsonPath("$.errors"),
                    hasNoJsonPath("$.warnings")
                )
            ));
    }


    @Test
    public void givenJudgeGrantsCostsClaim_whenCalledEndpoint_thenJudgeCostsClaimGrantedIsPopulated() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(JUDGE_COSTS_CLAIM_GRANTED, YES_VALUE);

        CcdCallbackRequest input = buildRequest(caseData);

        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content(convertObjectToJsonString(input))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        allOf(
                                isJson(),
                                hasJsonPath("$.data.JudgeCostsDecision", is(YES_VALUE)),
                                hasJsonPath("$.data.JudgeCostsClaimGranted", is(YES_VALUE)),
                                hasNoJsonPath("$.errors"),
                                hasNoJsonPath("$.warnings")
                        )
                ));
    }

    @Test
    public void givenJudgeCostsClaimIsNotGranted_whenCalledEndpoint_thenOtherFieldShouldBeRemovedI() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(JUDGE_COSTS_CLAIM_GRANTED, NO_VALUE);
        caseData.put(JUDGE_WHO_PAYS_COSTS, "Respondent");
        caseData.put(JUDGE_TYPE_COSTS_DECISION, "Half");
        caseData.put(JUDGE_COSTS_ADDITIONAL_INFO, "Additional info");

        CcdCallbackRequest input = buildRequest(caseData);

        webClient.perform(post(API_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content(convertObjectToJsonString(input))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        allOf(
                                isJson(),
                                hasJsonPath("$.data.JudgeCostsDecision", is(YES_VALUE)),
                                hasJsonPath("$.data.JudgeCostsClaimGranted", is(NO_VALUE)),
                                hasNoJsonPath("$.data.JudgeWhoPaysCosts"),
                                hasNoJsonPath("$.data.JudgeTypeCostsDecision"),
                                hasNoJsonPath("$.data.JudgeCostsOrderAdditionalInfo"),
                                hasNoJsonPath("$.errors"),
                                hasNoJsonPath("$.warnings")
                        )
                ));
    }

}
