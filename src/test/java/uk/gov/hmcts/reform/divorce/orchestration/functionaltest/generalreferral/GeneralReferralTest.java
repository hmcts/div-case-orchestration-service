package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.generalreferral;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceGeneralReferral;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static java.time.LocalDate.now;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.ALTERNATIVE_SERVICE_MEDIUM;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.FEE_AMOUNT_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_APPLICATION_ADDED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_APPLICATION_FROM;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_APPLICATION_REFERRAL_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRALS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_DECISION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_DECISION_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_FEE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_PAYMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRAL_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_ALTERNATIVE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_DECISION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_DECISION_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_FROM;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_PAYMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_REFERRAL_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.GeneralReferralUtil.buildCallbackRequest;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class GeneralReferralTest extends MockedFunctionalTest {

    private static final String API_URL = "/general-referral";

    @Autowired
    private MockMvc webClient;

    private CcdCallbackRequest ccdCallbackRequest;

    @Test
    public void givenCaseData_whenGeneralReferralFee_Yes_ThenState_Is_AwaitingGeneralReferralPayment() throws Exception {
        Map<String, Object> caseData = buildCaseData(YES_VALUE);

        ccdCallbackRequest = buildCallbackRequest(caseData, null);

        runTestForParams(YES_VALUE, CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT, 1);
    }

    @Test
    public void givenCaseData_whenGeneralReferralFee_No_ThenState_Is_AwaitingGeneralConsideration() throws Exception {
        Map<String, Object> caseData = buildCaseData(NO_VALUE);

        ccdCallbackRequest = buildCallbackRequest(caseData, null);

        runTestForParams(NO_VALUE, CcdStates.AWAITING_GENERAL_CONSIDERATION, 1);
    }

    @Test
    public void givenState_AwaitingGeneralReferralPayment_WhenGeneralReferralFee_No_ThenState_Is_AwaitingGeneralConsideration()
        throws Exception {
        Map<String, Object> caseData = buildCaseData(NO_VALUE);
        ccdCallbackRequest = buildCallbackRequest(caseData, CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT);

        runTestForParams(NO_VALUE, CcdStates.AWAITING_GENERAL_CONSIDERATION, 1);
    }

    @Test
    public void giveState_AwaitingGeneralConsideration_WhenGeneralReferralFee_Yes_ThenState_Is_AwaitingGeneralReferralPayment()
        throws Exception {
        Map<String, Object> caseData = buildCaseData(YES_VALUE);

        ccdCallbackRequest = buildCallbackRequest(caseData, CcdStates.AWAITING_GENERAL_CONSIDERATION);

        runTestForParams(YES_VALUE, CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT, 1);
    }

    @Test
    public void moreThanOneElementInGeneralReferralsCollection() throws Exception {
        Map<String, Object> caseData = buildCaseData(YES_VALUE);

        ccdCallbackRequest = buildCallbackRequest(addElementToCollection(caseData), CcdStates.AWAITING_GENERAL_CONSIDERATION);

        runTestForParams(YES_VALUE, CcdStates.AWAITING_GENERAL_REFERRAL_PAYMENT, 2);
    }

    private void runTestForParams(String referralFeeValue, String newCaseState, int size) throws Exception {
        int index = size - 1;
        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    hasNoJsonKeyInResponse(GENERAL_REFERRAL_FEE),
                    hasNoJsonKeyInResponse(GENERAL_REFERRAL_DECISION_DATE),
                    hasNoJsonKeyInResponse(GENERAL_REFERRAL_REASON),
                    hasNoJsonKeyInResponse(GENERAL_REFERRAL_TYPE),
                    hasNoJsonKeyInResponse(GENERAL_REFERRAL_DETAILS),
                    hasNoJsonKeyInResponse(GENERAL_REFERRAL_PAYMENT_TYPE),
                    hasNoJsonKeyInResponse(GENERAL_REFERRAL_DECISION),
                    hasNoJsonKeyInResponse(GENERAL_REFERRAL_DECISION_REASON),
                    hasNoJsonKeyInResponse(GENERAL_APPLICATION_ADDED_DATE),
                    hasNoJsonKeyInResponse(GENERAL_APPLICATION_FROM),
                    hasNoJsonKeyInResponse(GENERAL_APPLICATION_REFERRAL_DATE),
                    hasNoJsonKeyInResponse(ALTERNATIVE_SERVICE_MEDIUM),
                    hasNoJsonKeyInResponse(FEE_AMOUNT_WITHOUT_NOTICE),
                    hasJsonPath("$.data.GeneralReferrals", hasSize(size)),
                    assertFieldInResponseIs(index, GENERAL_REFERRAL_FEE, referralFeeValue),
                    assertFieldInResponseIs(index, GENERAL_REFERRAL_DECISION_DATE, TEST_DECISION_DATE),
                    assertFieldInResponseIs(index, GENERAL_REFERRAL_REASON, TEST_REASON),
                    assertFieldInResponseIs(index, GENERAL_REFERRAL_TYPE, TEST_TYPE),
                    assertFieldInResponseIs(index, GENERAL_REFERRAL_DETAILS, TEST_DETAILS),
                    assertFieldInResponseIs(index, GENERAL_REFERRAL_PAYMENT_TYPE, TEST_PAYMENT_TYPE),
                    assertFieldInResponseIs(index, GENERAL_REFERRAL_DECISION, TEST_DECISION),
                    assertFieldInResponseIs(index, GENERAL_REFERRAL_DECISION_REASON, TEST_DECISION_REASON),
                    assertFieldInResponseIs(index, GENERAL_APPLICATION_ADDED_DATE, DateUtils.formatDateFromLocalDate(now())),
                    assertFieldInResponseIs(index, GENERAL_APPLICATION_FROM, TEST_FROM),
                    assertFieldInResponseIs(index, GENERAL_APPLICATION_REFERRAL_DATE, TEST_REFERRAL_DATE),
                    assertFieldInResponseIs(index, ALTERNATIVE_SERVICE_MEDIUM, TEST_ALTERNATIVE),
                    hasJsonPath("$.state", is(newCaseState))
                )
            ));
    }

    private Map<String, Object> addElementToCollection(Map<String, Object> caseData) {
        List<CollectionMember<DivorceGeneralReferral>> collection = new ArrayList<>();
        collection.add(new CollectionMember<>());

        caseData.put(GENERAL_REFERRALS, collection);

        return caseData;
    }

    private Map<String, Object> buildCaseData(String value) {
        Map<String, Object> caseData = GeneralReferralDataTaskTest.buildCaseData();
        caseData.put(GENERAL_REFERRAL_FEE, value);
        caseData.remove(GENERAL_APPLICATION_ADDED_DATE);

        return caseData;
    }

    private Matcher<? super Object> hasNoJsonKeyInResponse(String key) {
        String path = String.format("$.data.%s", key);

        return hasNoJsonPath(path);
    }

    private Matcher<? super Object> assertFieldInResponseIs(int newlyAddedItemIndex, String key, Object expected) {
        String path = String.format("$.data.GeneralReferrals[%s].value.%s", newlyAddedItemIndex, key);

        return hasJsonPath(path, is(expected));
    }
}
