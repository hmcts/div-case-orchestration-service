package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.FeatureToggleServiceImpl;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED_REJECT_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

public class DecreeNisiStateTest extends MockedFunctionalTest {

    private static final String API_URL = "/dn-about-to-be-granted-state";
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

        webClient.perform(post(API_URL).header(AUTHORIZATION, AUTH_TOKEN).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(YES_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(AWAITING_PRONOUNCEMENT))
                ))
            )));
    }

    @Test
    public void shouldReturnCaseDataPlusClarificationDocument_AndState_WhenDN_NotGranted_AndDnRefusedForMoreInfo() throws Exception {

        Map<String, Object> caseData = ImmutableMap.of(
            DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE,
            REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE
        );

        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            ImmutableMap.of(
                ID, TEST_CASE_ID,
                CCD_CASE_DATA_FIELD, caseData
            )
        ));

        webClient.perform(post(API_URL).header(AUTHORIZATION, AUTH_TOKEN).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(NO_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(AWAITING_CLARIFICATION))
                ))
            )));
    }

    @Test
    public void givenRejection_whenMakeDecision_thenReturnRightState() throws Exception {

        Map<String, Object> caseData = ImmutableMap.of(
            DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE,
            REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION
        );
        String inputJson = JSONObject.valueToString(singletonMap(CASE_DETAILS_JSON_KEY,
            ImmutableMap.of(
                ID, TEST_CASE_ID,
                CCD_CASE_DATA_FIELD, caseData
            )
        ));

        webClient.perform(post(API_URL).header(AUTHORIZATION, AUTH_TOKEN).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(NO_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(DN_REFUSED)),
                    hasJsonPath(REFUSAL_DECISION_CCD_FIELD, equalTo(DN_REFUSED_REJECT_OPTION))
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
        webClient.perform(post(API_URL).header(AUTHORIZATION, AUTH_TOKEN).content(inputJson).contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                hasJsonPath(CCD_RESPONSE_DATA_FIELD, allOf(
                    hasJsonPath(DECREE_NISI_GRANTED_CCD_FIELD, equalTo(NO_VALUE)),
                    hasJsonPath(STATE_CCD_FIELD, equalTo(AWAITING_CLARIFICATION))
                ))
            )));
    }

    private void setDnFeature(Boolean enableFeature) {
        featureToggleService.getToggle().put(Features.DN_REFUSAL.getName(), enableFeature.toString());
    }
}