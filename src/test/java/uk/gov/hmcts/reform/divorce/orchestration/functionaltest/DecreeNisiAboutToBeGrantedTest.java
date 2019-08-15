package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.FeatureToggleServiceImpl;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_DECISION_DATE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_OUTCOME_FLAG_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDNDecisionStateTask.DN_REFUSED_REJECT_OPTION;

@RunWith(SpringRunner.class)
public class DecreeNisiAboutToBeGrantedTest extends MockedFunctionalTest {

    private static final String API_URL = "/dn-about-to-be-granted";
    private static final String CCD_RESPONSE_DATA_FIELD = "data";

    @Autowired
    private MockMvc webClient;

    @Autowired
    private CcdUtil ccdUtil;

    @Autowired
    private FeatureToggleServiceImpl featureToggleService;

    @Before
    public void setup() {
        setDnFeature(true);
    }

    @Test
    public void shouldReturnCaseDataPlusDnGrantedDate_AndState_WhenDNGranted_AndCostsOrderGranted() throws Exception {
        HashMap<Object, Object> caseData = new HashMap<>();
        caseData.put(DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE);
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE);
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE);
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD, caseData)
        ));

        webClient.perform(post(API_URL).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(YES_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(AWAITING_PRONOUNCEMENT)),
                    hasJsonPath(WHO_PAYS_COSTS_CCD_FIELD, equalTo(WHO_PAYS_CCD_CODE_FOR_RESPONDENT)),
                    hasJsonPath(DN_OUTCOME_FLAG_CCD_FIELD, equalTo(YES_VALUE)),
                    hasJsonPath(DN_DECISION_DATE_FIELD, equalTo(ccdUtil.getCurrentDateCcdFormat()))
                ))
            )));
    }

    @Test
    public void shouldReturnCaseDataPlusDnGrantedDate_AndState_WhenDNGranted_ButCostsOrderNotGranted() throws Exception {
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD,
                singletonMap(DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE)
            )
        ));

        webClient.perform(post(API_URL).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(YES_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(AWAITING_PRONOUNCEMENT)),
                    hasJsonPath(DN_OUTCOME_FLAG_CCD_FIELD, equalTo(YES_VALUE)),
                    hasNoJsonPath(WHO_PAYS_COSTS_CCD_FIELD),
                    hasJsonPath(DN_DECISION_DATE_FIELD, equalTo(ccdUtil.getCurrentDateCcdFormat()))
                ))
            )));
    }

    @Test
    public void shouldReturnCaseDataPlusDnGrantedDate_AndState_WhenDN_NotGranted() throws Exception {
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD,
                singletonMap(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE)
            )
        ));

        webClient.perform(post(API_URL).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(NO_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(AWAITING_CLARIFICATION)),
                    hasJsonPath(DN_DECISION_DATE_FIELD, equalTo(ccdUtil.getCurrentDateCcdFormat())),
                    hasNoJsonPath(WHO_PAYS_COSTS_CCD_FIELD),
                    hasNoJsonPath(DN_OUTCOME_FLAG_CCD_FIELD)
                ))
            )));
    }

    @Test
    public void givenCostDecision_whenNoClaimCost_thenReturnError() throws Exception {
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD,
                ImmutableMap.of(DIVORCE_COSTS_CLAIM_CCD_FIELD, NO_VALUE,
                DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE,
                DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE)
            ))
        );
        webClient.perform(post(API_URL).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("errors", contains("Cost decision can only be made if cost has been requested")
                ))
            ));
    }

    @Test
    public void givenNoCostDecision_whenClaimCost_thenReturnError() throws Exception {
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD,
                ImmutableMap.of(DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE,
                    DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE)
            ))
        );
        webClient.perform(post(API_URL).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath("errors", contains("Cost decision expected")
                ))
            ));
    }

    @Test
    public void givenRejection_whenMakeDecision_thenReturnRightState() throws Exception {
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD,
                ImmutableMap.of(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION,
                    DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE)
            ))
        );
        webClient.perform(post(API_URL).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(NO_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(DN_REFUSED)),
                    hasJsonPath(DN_DECISION_DATE_FIELD, equalTo(ccdUtil.getCurrentDateCcdFormat()))
                ))
            )));
    }

    @Test
    public void givenRejection_andToggleDisabled_whenMakeDecision_thenReturnRightState() throws Exception {
        setDnFeature(false);

        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            singletonMap(CCD_CASE_DATA_FIELD,
                ImmutableMap.of(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION,
                    DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE)
            ))
        );
        webClient.perform(post(API_URL).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(NO_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(AWAITING_CLARIFICATION)),
                    hasJsonPath(DN_DECISION_DATE_FIELD, equalTo(ccdUtil.getCurrentDateCcdFormat()))
                ))
            )));
    }

    private void setDnFeature(Boolean enableFeature) {
        featureToggleService.getToggle().put(Features.DN_REFUSAL.getName(), enableFeature.toString());
    }
}