package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.generalreferral;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.GeneralReferralUtil.buildCallbackRequest;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

@WebMvcTest
public class ReturnToStateBeforeGeneralReferralTest extends MockedFunctionalTest {

    private static final String API_URL = "/return-to-state-before-general-referral";

    @Autowired
    private MockMvc webClient;

    private CcdCallbackRequest ccdCallbackRequest;

    @Test
    public void givenState_AwaitingGeneralConsideration_WhenGeneralReferralFee_Yes_ThenState_Is_AwaitingGeneralReferralPayment() throws Exception {
        ccdCallbackRequest = buildCallbackRequest(
            ImmutableMap.of(
                CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE, "caseStateBeforeGeneralReferral"),
                TEST_STATE);

        performRequestAndValidateStateIs("caseStateBeforeGeneralReferral");
    }

    @Test
    public void givenNoPreviousCaseState_WhenStateBeforeGeneralReferral_ThenReturns_Error() throws Exception {
        ccdCallbackRequest = buildCallbackRequest(
            emptyMap(),
            TEST_STATE);

        performRequestAndValidateHasError(
            format("Could not evaluate value of mandatory property \"%s\"", CcdFields.GENERAL_REFERRAL_PREVIOUS_CASE_STATE));
    }

    private void performRequestAndValidateStateIs(String newCaseState) throws Exception {
        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasJsonPath("$.state", is(newCaseState)))
            ));
    }

    private void performRequestAndValidateHasError(String errorMessage) throws Exception {
        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasNoJsonPath("$.data"),
                    hasJsonPath("$.errors",
                        hasItem(errorMessage)
                    ))
            ));
    }

}
