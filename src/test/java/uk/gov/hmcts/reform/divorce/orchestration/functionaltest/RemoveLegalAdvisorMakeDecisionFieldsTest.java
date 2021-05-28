package uk.gov.hmcts.reform.divorce.orchestration.functionaltest;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matcher;
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
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_ADDITIONAL_INFO_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TYPE_COSTS_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class RemoveLegalAdvisorMakeDecisionFieldsTest extends MockedFunctionalTest {

    private static final String API_URL = "/remove-la-make-decision-fields";
    public static final String THIS_FIELD_WILL_BE_RETURNED = "this-field-will-be-returned";

    @Autowired
    private MockMvc webClient;

    @Test
    public void givenCase_whenRemoveLegalAdvisorMakeDecisionFields_thenFieldsRemoved() throws Exception {
        Map<String, Object> caseData = new HashMap<>(ImmutableMap.of(
            DECREE_NISI_GRANTED_CCD_FIELD, "Yes",
            DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "Yes",
            WHO_PAYS_COSTS_CCD_FIELD, "respondent",
            TYPE_COSTS_DECISION_CCD_FIELD, "info",
            COSTS_ORDER_ADDITIONAL_INFO_CCD_FIELD, "some text"
        ));
        caseData.put(THIS_FIELD_WILL_BE_RETURNED, "some text");
        CcdCallbackRequest ccdCallbackRequest = buildRequest(caseData);

        webClient.perform(post(API_URL)
            .header(AUTHORIZATION, AUTH_TOKEN)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(allOf(
                isJson(),
                assertThereIsNoFieldReturned(DECREE_NISI_GRANTED_CCD_FIELD),
                assertThereIsNoFieldReturned(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD),
                assertThereIsNoFieldReturned(WHO_PAYS_COSTS_CCD_FIELD),
                assertThereIsNoFieldReturned(TYPE_COSTS_DECISION_CCD_FIELD),
                assertThereIsNoFieldReturned(COSTS_ORDER_ADDITIONAL_INFO_CCD_FIELD),
                hasJsonPath("$.data." + THIS_FIELD_WILL_BE_RETURNED),
                hasNoJsonPath("$.errors"),
                hasNoJsonPath("$.warnings")
            )));
    }

    private Matcher<? super Object> assertThereIsNoFieldReturned(String field) {
        return hasNoJsonPath("$.data." + field);
    }

    private CcdCallbackRequest buildRequest(Map<String, Object> caseData) {
        return CcdCallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .caseData(caseData)
                .build())
            .build();
    }
}
