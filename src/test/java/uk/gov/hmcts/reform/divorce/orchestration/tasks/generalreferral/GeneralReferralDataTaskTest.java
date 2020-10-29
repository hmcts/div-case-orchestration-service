package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceGeneralReferral;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRALS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class GeneralReferralDataTaskTest {

    public static final String TEST_FEE = YES_VALUE;
    public static final String TEST_DECISION_DATE = "2010-10-10";
    public static final String TEST_REASON = "GeneralReferralReason";
    public static final String TEST_TYPE = "GeneralReferralType";
    public static final String TEST_DETAILS = "GeneralReferralDetails";
    public static final String TEST_PAYMENT_TYPE = "GeneralReferralPaymentType";
    public static final String TEST_DECISION = "GeneralReferralDecision";
    public static final String TEST_DECISION_REASON = "GeneralReferralDecisionReason";
    public static final String TEST_ADDED_DATE = "1999-09-09";
    public static final String TEST_FROM = "GeneralApplicationFrom";
    public static final String TEST_REFERRAL_DATE = "2000-01-01";
    public static final String TEST_ALTERNATIVE = "AlternativeServiceMedium";

    @InjectMocks
    private GeneralReferralDataTask generalReferralDataTask;

    @Test
    public void shouldExecuteAndAddElementToNewCollection() {
        Map<String, Object> caseData = buildCaseData();
        int originalSize = caseData.size();

        Map<String, Object> output = generalReferralDataTask.execute(context(), caseData);

        List<CollectionMember<DivorceGeneralReferral>> collectionMembers = (List) output.get(GENERAL_REFERRALS);
        DivorceGeneralReferral generalReferral = collectionMembers.get(0).getValue();

        assertThat(output.size(), is(originalSize + 1));
        assertGeneralReferralIsAsExpected(generalReferral);
    }

    @Test
    public void shouldExecuteAndAddAnotherElementToExistingCollection() {
        Map<String, Object> caseData = buildCaseData();
        List<CollectionMember<DivorceGeneralReferral>> originalMemberList = new ArrayList<>(asList(buildCollectionMember()));
        caseData.put(GENERAL_REFERRALS, originalMemberList);

        Map<String, Object> output = generalReferralDataTask.execute(context(), caseData);

        List<CollectionMember<DivorceGeneralReferral>> collectionMembers = (List) output.get(GENERAL_REFERRALS);
        DivorceGeneralReferral generalReferral = collectionMembers.get(1).getValue();

        assertThat(collectionMembers.size(), is(originalMemberList.size() + 1));
        assertGeneralReferralIsAsExpected(generalReferral);
    }

    @Test
    public void shouldExecuteAndNotIncludeConditionalValues() {
        Map<String, Object> caseData = buildCaseData();
        caseData.replace(CcdFields.GENERAL_REFERRAL_FEE, NO_VALUE);
        caseData.replace(CcdFields.GENERAL_REFERRAL_REASON,  TEST_REASON);
        caseData.replace(CcdFields.GENERAL_REFERRAL_TYPE, TEST_TYPE);

        List<CollectionMember<DivorceGeneralReferral>> originalMemberList = new ArrayList<>(asList(buildCollectionMember()));
        caseData.put(GENERAL_REFERRALS, originalMemberList);

        Map<String, Object> output = generalReferralDataTask.execute(context(), caseData);

        List<CollectionMember<DivorceGeneralReferral>> collectionMembers = (List) output.get(GENERAL_REFERRALS);
        DivorceGeneralReferral generalReferral = collectionMembers.get(1).getValue();

        assertThat(collectionMembers.size(), is(originalMemberList.size() + 1));
        assertGeneralReferralDoesNotContainConditionalFields(generalReferral);
    }

    private void assertGeneralReferralIsAsExpected(DivorceGeneralReferral generalReferral) {
        DivorceGeneralReferral  expectedGeneralReferral = DivorceGeneralReferral.builder()
            .generalReferralReason(CcdFields.GENERAL_APPLICATION_REFERRAL)
            .generalReferralDecisionReason(TEST_DECISION_REASON)
            .generalReferralDecision(TEST_DECISION)
            .generalApplicationAddedDate(TEST_ADDED_DATE)
            .generalReferralDecisionDate(TEST_DECISION_DATE)
            .generalReferralDetails(TEST_DETAILS)
            .generalReferralFee(TEST_FEE)
            .generalReferralType(CcdFields.ALTERNATIVE_SERVICE_APPLICATION)
            .generalApplicationReferralDate(TEST_REFERRAL_DATE)
            .alternativeServiceMedium(TEST_ALTERNATIVE)
            .generalApplicationFrom(TEST_FROM)
            .generalReferralPaymentType(TEST_PAYMENT_TYPE)
            .build();

        assertThat(generalReferral, is(expectedGeneralReferral));
    }

    private void assertGeneralReferralDoesNotContainConditionalFields(DivorceGeneralReferral generalReferral) {
        DivorceGeneralReferral  expectedGeneralReferral = DivorceGeneralReferral.builder()
            .generalReferralReason(TEST_REASON)
            .generalReferralDecisionReason(TEST_DECISION_REASON)
            .generalReferralDecision(TEST_DECISION)
            .generalApplicationAddedDate(TEST_ADDED_DATE)
            .generalReferralDecisionDate(TEST_DECISION_DATE)
            .generalReferralDetails(TEST_DETAILS)
            .generalReferralFee(NO_VALUE)
            .generalReferralType(TEST_TYPE)
            .generalApplicationReferralDate(TEST_REFERRAL_DATE)
            .build();

        assertThat(generalReferral, is(expectedGeneralReferral));
    }

    public static Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(CcdFields.GENERAL_REFERRAL_FEE, TEST_FEE);
        caseData.put(CcdFields.GENERAL_REFERRAL_DECISION_DATE, TEST_DECISION_DATE);
        caseData.put(CcdFields.GENERAL_REFERRAL_REASON, CcdFields.GENERAL_APPLICATION_REFERRAL);
        caseData.put(CcdFields.GENERAL_REFERRAL_TYPE, CcdFields.ALTERNATIVE_SERVICE_APPLICATION);
        caseData.put(CcdFields.GENERAL_REFERRAL_DETAILS, TEST_DETAILS);
        caseData.put(CcdFields.GENERAL_REFERRAL_PAYMENT_TYPE, TEST_PAYMENT_TYPE);
        caseData.put(CcdFields.GENERAL_REFERRAL_DECISION, TEST_DECISION);
        caseData.put(CcdFields.GENERAL_REFERRAL_DECISION_REASON, TEST_DECISION_REASON);
        caseData.put(CcdFields.GENERAL_APPLICATION_ADDED_DATE, TEST_ADDED_DATE);
        caseData.put(CcdFields.GENERAL_APPLICATION_FROM, TEST_FROM);
        caseData.put(CcdFields.GENERAL_APPLICATION_REFERRAL_DATE, TEST_REFERRAL_DATE);
        caseData.put(CcdFields.ALTERNATIVE_SERVICE_MEDIUM, TEST_ALTERNATIVE);

        return caseData;
    }

    public static CollectionMember<DivorceGeneralReferral> buildCollectionMember() {
        CollectionMember<DivorceGeneralReferral> collectionMember = new CollectionMember<>();
        collectionMember.setValue(DivorceGeneralReferral.builder().build());

        return collectionMember;
    }
}
