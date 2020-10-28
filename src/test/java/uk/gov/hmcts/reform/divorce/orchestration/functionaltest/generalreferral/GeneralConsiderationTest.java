package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.generalreferral;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
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
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_ADDED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_ALTERNATIVE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_DECISION_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_FROM;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_PAYMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_REFERRAL_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral.GeneralReferralDataTaskTest.TEST_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.GeneralReferralUtil.buildCallbackRequest;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public class GeneralConsiderationTest extends MockedFunctionalTest {

    private static final String API_URL = "/general-consideration";

    @Autowired
    private MockMvc webClient;

    private CcdCallbackRequest ccdCallbackRequest;

    @Test
    public void moreThanOneElementInGeneralReferralsCollection() throws Exception {
        Map<String, Object> caseData = buildCaseData(
            YES_VALUE,
            CcdFields.GENERAL_APPLICATION_REFERRAL,
            CcdFields.ALTERNATIVE_SERVICE_APPLICATION);

        ccdCallbackRequest = buildCallbackRequest(addElementToCollection(caseData), CcdStates.AWAITING_GENERAL_CONSIDERATION);

        runTestForParams(2, true);
    }

    @Test
    public void testConditionalFieldsNotAddedToGeneralReferral() throws Exception {
        Map<String, Object> caseData = buildCaseData(
            NO_VALUE,
            TEST_REASON,
            TEST_TYPE);

        ccdCallbackRequest = buildCallbackRequest(addElementToCollection(caseData), CcdStates.AWAITING_GENERAL_CONSIDERATION);

        runTestForParams(2, false);
    }

    private void runTestForParams(int size, boolean isWithConditionalFields) throws Exception {
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
                    assertKeysNotInResponse(),
                    hasJsonPath("$.data.GeneralReferrals", hasSize(size)),
                    assertFieldValues(index, isWithConditionalFields)
                )
            ));
    }

    private Matcher<? super Object> assertKeysNotInResponse() {
        return allOf(
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
            hasNoJsonKeyInResponse(FEE_AMOUNT_WITHOUT_NOTICE)
        );
    }

    private Matcher<? super Object> assertFieldValues(int index, boolean isWithConditionalFields) {
        if (isWithConditionalFields) {
            return assertFieldValuesWithAllConditionalFields(index);
        }
        return assertFieldValuesWithoutConditionalFields(index);
    }

    private Matcher<? super Object> assertFieldValuesWithAllConditionalFields(int index) {
        return allOf(
            assertFieldInResponseIs(index, GENERAL_REFERRAL_FEE, YES_VALUE),
            assertFieldInResponseIs(index, GENERAL_REFERRAL_DECISION_DATE, DateUtils.formatDateFromLocalDate(now())),
            assertFieldInResponseIs(index, GENERAL_REFERRAL_REASON, CcdFields.GENERAL_APPLICATION_REFERRAL),
            assertFieldInResponseIs(index, GENERAL_REFERRAL_TYPE, CcdFields.ALTERNATIVE_SERVICE_APPLICATION),
            assertFieldInResponseIs(index, GENERAL_REFERRAL_DETAILS, TEST_DETAILS),
            assertFieldInResponseIs(index, GENERAL_REFERRAL_PAYMENT_TYPE, TEST_PAYMENT_TYPE),
            assertFieldInResponseIs(index, GENERAL_REFERRAL_DECISION, TEST_DECISION),
            assertFieldInResponseIs(index, GENERAL_REFERRAL_DECISION_REASON, TEST_DECISION_REASON),
            assertFieldInResponseIs(index, GENERAL_APPLICATION_ADDED_DATE, TEST_ADDED_DATE),
            assertFieldInResponseIs(index, GENERAL_APPLICATION_FROM, TEST_FROM),
            assertFieldInResponseIs(index, GENERAL_APPLICATION_REFERRAL_DATE, TEST_REFERRAL_DATE),
            assertFieldInResponseIs(index, ALTERNATIVE_SERVICE_MEDIUM, TEST_ALTERNATIVE)
        );
    }

    private Matcher<? super Object> assertFieldValuesWithoutConditionalFields(int index) {
        return allOf(
            assertFieldInResponseIs(index, GENERAL_REFERRAL_FEE, NO_VALUE),
            assertFieldInResponseIs(index, GENERAL_REFERRAL_DECISION_DATE, DateUtils.formatDateFromLocalDate(now())),
            assertFieldInResponseIs(index, GENERAL_REFERRAL_REASON, TEST_REASON),
            assertFieldInResponseIs(index, GENERAL_REFERRAL_TYPE, TEST_TYPE),
            assertFieldInResponseIs(index, GENERAL_REFERRAL_DETAILS, TEST_DETAILS),
            assertFieldInResponseIs(index, GENERAL_REFERRAL_DECISION, TEST_DECISION),
            assertFieldInResponseIs(index, GENERAL_REFERRAL_DECISION_REASON, TEST_DECISION_REASON),
            assertFieldInResponseIs(index, GENERAL_APPLICATION_ADDED_DATE, TEST_ADDED_DATE),
            assertFieldInResponseIs(index, GENERAL_APPLICATION_REFERRAL_DATE, TEST_REFERRAL_DATE),
            assertFieldIsNotInResponse(index, GENERAL_REFERRAL_PAYMENT_TYPE),
            assertFieldIsNotInResponse(index, GENERAL_APPLICATION_FROM),
            assertFieldIsNotInResponse(index, ALTERNATIVE_SERVICE_MEDIUM)
        );
    }

    private Map<String, Object> addElementToCollection(Map<String, Object> caseData) {
        List<CollectionMember<DivorceGeneralReferral>> collection = new ArrayList<>();
        collection.add(new CollectionMember<>());

        caseData.put(GENERAL_REFERRALS, collection);

        return caseData;
    }

    private Map<String, Object> buildCaseData(String isFeeRequired, String referralReason, String referralType) {
        Map<String, Object> caseData = GeneralReferralDataTaskTest.buildCaseData();
        caseData.put(GENERAL_REFERRAL_FEE, isFeeRequired);
        caseData.put(GENERAL_REFERRAL_REASON, referralReason);
        caseData.put(GENERAL_REFERRAL_TYPE, referralType);

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

    private Matcher<? super Object> assertFieldIsNotInResponse(int newlyAddedItemIndex, String key) {
        String path = String.format("$.data.GeneralReferrals[%s].value.%s", newlyAddedItemIndex, key);

        return hasNoJsonPath(path);
    }
}
