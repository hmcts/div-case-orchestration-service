package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.generalreferral;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest
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

        int expectedListSize = 2;
        int indexOfItemToCheck = expectedListSize - 1;

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    assertFieldsToRemoveNotInResponse(),
                    hasJsonPath("$.data.GeneralReferrals", hasSize(expectedListSize)),
                    assertFieldValuesWithAllConditionalFields(indexOfItemToCheck)
                )
            ));
    }

    @Test
    public void testConditionalFieldsNotAddedToGeneralReferral() throws Exception {
        Map<String, Object> caseData = buildCaseData(
            NO_VALUE,
            TEST_REASON,
            TEST_TYPE);

        ccdCallbackRequest = buildCallbackRequest(addElementToCollection(caseData), CcdStates.AWAITING_GENERAL_CONSIDERATION);

        int expectedListSize = 2;
        int indexOfItemToCheck = expectedListSize - 1;

        webClient.perform(post(API_URL)
            .content(convertObjectToJsonString(ccdCallbackRequest))
            .header(AUTHORIZATION, AUTH_TOKEN)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(
                allOf(
                    isJson(),
                    assertFieldsToRemoveNotInResponse(),
                    hasJsonPath("$.data.GeneralReferrals", hasSize(expectedListSize)),
                    assertFieldValuesWithoutConditionalFields(indexOfItemToCheck)
                )
            ));
    }

    private Map<String, Object> buildCaseData(String isFeeRequired, String referralReason, String referralType) {
        Map<String, Object> caseData = GeneralReferralDataTaskTest.buildCaseData();
        caseData.put(GENERAL_REFERRAL_FEE, isFeeRequired);
        caseData.put(GENERAL_REFERRAL_REASON, referralReason);
        caseData.put(GENERAL_REFERRAL_TYPE, referralType);

        return caseData;
    }

    private Map<String, Object> addElementToCollection(Map<String, Object> caseData) {
        List<CollectionMember<DivorceGeneralReferral>> collection = new ArrayList<>();
        collection.add(new CollectionMember<>());

        caseData.put(GENERAL_REFERRALS, collection);

        return caseData;
    }

    private Matcher<? super Object> assertFieldsToRemoveNotInResponse() {
        return hasJsonPath("$.data", allOf(
            hasNoJsonPath(GENERAL_REFERRAL_FEE),
            hasNoJsonPath(GENERAL_REFERRAL_DECISION_DATE),
            hasNoJsonPath(GENERAL_REFERRAL_REASON),
            hasNoJsonPath(GENERAL_REFERRAL_TYPE),
            hasNoJsonPath(GENERAL_REFERRAL_DETAILS),
            hasNoJsonPath(GENERAL_REFERRAL_PAYMENT_TYPE),
            hasNoJsonPath(GENERAL_REFERRAL_DECISION),
            hasNoJsonPath(GENERAL_REFERRAL_DECISION_REASON),
            hasNoJsonPath(GENERAL_APPLICATION_ADDED_DATE),
            hasNoJsonPath(GENERAL_APPLICATION_FROM),
            hasNoJsonPath(GENERAL_APPLICATION_REFERRAL_DATE),
            hasNoJsonPath(ALTERNATIVE_SERVICE_MEDIUM),
            hasNoJsonPath(FEE_AMOUNT_WITHOUT_NOTICE)
        ));
    }

    private Matcher<? super Object> assertFieldValuesWithAllConditionalFields(int index) {
        String generalReferralPath = String.format("$.data.GeneralReferrals[%s].value", index);
        return hasJsonPath(generalReferralPath, allOf(
            hasJsonPath(GENERAL_REFERRAL_FEE, is(YES_VALUE)),
            hasJsonPath(GENERAL_REFERRAL_DECISION_DATE, is(DateUtils.formatDateFromLocalDate(now()))),
            hasJsonPath(GENERAL_REFERRAL_REASON, is(CcdFields.GENERAL_APPLICATION_REFERRAL)),
            hasJsonPath(GENERAL_REFERRAL_TYPE, is(CcdFields.ALTERNATIVE_SERVICE_APPLICATION)),
            hasJsonPath(GENERAL_REFERRAL_DETAILS, is(TEST_DETAILS)),
            hasJsonPath(GENERAL_REFERRAL_PAYMENT_TYPE, is(TEST_PAYMENT_TYPE)),
            hasJsonPath(GENERAL_REFERRAL_DECISION, is(TEST_DECISION)),
            hasJsonPath(GENERAL_REFERRAL_DECISION_REASON, is(TEST_DECISION_REASON)),
            hasJsonPath(GENERAL_APPLICATION_ADDED_DATE, is(TEST_ADDED_DATE)),
            hasJsonPath(GENERAL_APPLICATION_FROM, is(TEST_FROM)),
            hasJsonPath(GENERAL_APPLICATION_REFERRAL_DATE, is(TEST_REFERRAL_DATE)),
            hasJsonPath(ALTERNATIVE_SERVICE_MEDIUM, is(TEST_ALTERNATIVE))
        ));
    }

    private Matcher<? super Object> assertFieldValuesWithoutConditionalFields(int index) {
        String generalReferralPath = String.format("$.data.GeneralReferrals[%s].value", index);
        return hasJsonPath(generalReferralPath, allOf(
            hasJsonPath(GENERAL_REFERRAL_FEE, is(NO_VALUE)),
            hasJsonPath(GENERAL_REFERRAL_DECISION_DATE, is(DateUtils.formatDateFromLocalDate(now()))),
            hasJsonPath(GENERAL_REFERRAL_REASON, is(TEST_REASON)),
            hasJsonPath(GENERAL_REFERRAL_TYPE, is(TEST_TYPE)),
            hasJsonPath(GENERAL_REFERRAL_DETAILS, is(TEST_DETAILS)),
            hasJsonPath(GENERAL_REFERRAL_DECISION, is(TEST_DECISION)),
            hasJsonPath(GENERAL_REFERRAL_DECISION_REASON, is(TEST_DECISION_REASON)),
            hasJsonPath(GENERAL_APPLICATION_ADDED_DATE, is(TEST_ADDED_DATE)),
            hasJsonPath(GENERAL_APPLICATION_REFERRAL_DATE, is(TEST_REFERRAL_DATE)),
            hasNoJsonPath(GENERAL_REFERRAL_PAYMENT_TYPE),
            hasNoJsonPath(GENERAL_APPLICATION_FROM),
            hasNoJsonPath(ALTERNATIVE_SERVICE_MEDIUM)
        ));
    }
}
