package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceGeneralReferral;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_REFERRALS;
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
    @Mock
    private CcdUtil ccdUtil;

    @InjectMocks
    private GeneralReferralDataTask generalReferralDataTask;

    @Test
    public void shouldExecuteAndAddElementToNewCollection() {
        Map<String, Object> input = buildCaseData();
        int originalSize = input.size();

        when(ccdUtil.getListOfGeneralReferrals(input)).thenReturn(new ArrayList<>());

        Map<String, Object> output = generalReferralDataTask.execute(context(), input);

        List<CollectionMember<DivorceGeneralReferral>> collectionMembers = (List) output.get(GENERAL_REFERRALS);
        DivorceGeneralReferral generalReferral = collectionMembers.get(0).getValue();

        assertThat(output.size(), is(originalSize + 1));
        assertGeneralReferralIsCorrect(generalReferral);
    }

    @Test
    public void shouldExecuteAndAddAnotherElementToExistingCollection() {
        Map<String, Object> input = buildCaseData();
        List<CollectionMember<DivorceGeneralReferral>> memberList = new ArrayList<>(asList(buildCollectionMember()));

        when(ccdUtil.getListOfGeneralReferrals(input)).thenReturn(memberList);

        Map<String, Object> output = generalReferralDataTask.execute(context(), input);

        List<CollectionMember<DivorceGeneralReferral>> collectionMembers = (List) output.get(GENERAL_REFERRALS);

        assertThat(collectionMembers.size(), is(2));

        DivorceGeneralReferral generalReferral = collectionMembers.get(1).getValue();

        assertGeneralReferralIsCorrect(generalReferral);
    }

    private void assertGeneralReferralIsCorrect(DivorceGeneralReferral generalReferral) {
        assertThat(generalReferral.getGeneralReferralReason(), is(TEST_REASON));
        assertThat(generalReferral.getGeneralReferralDecisionReason(), is(TEST_DECISION_REASON));
        assertThat(generalReferral.getGeneralReferralDecision(), is(TEST_DECISION));
        assertThat(generalReferral.getAlternativeServiceMedium(), is(TEST_ALTERNATIVE));
        assertThat(generalReferral.getGeneralApplicationAddedDate(), is(TEST_ADDED_DATE));
        assertThat(generalReferral.getGeneralApplicationFrom(), is(TEST_FROM));
        assertThat(generalReferral.getGeneralApplicationReferralDate(), is(TEST_REFERRAL_DATE));
        assertThat(generalReferral.getGeneralReferralDecisionDate(), is(TEST_DECISION_DATE));
        assertThat(generalReferral.getGeneralReferralDetails(), is(TEST_DETAILS));
        assertThat(generalReferral.getGeneralReferralFee(), is(TEST_FEE));
        assertThat(generalReferral.getGeneralReferralType(), is(TEST_TYPE));
        assertThat(generalReferral.getGeneralReferralPaymentType(), is(TEST_PAYMENT_TYPE));

    }

    public static Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(CcdFields.GENERAL_REFERRAL_FEE, TEST_FEE);
        caseData.put(CcdFields.GENERAL_REFERRAL_DECISION_DATE, TEST_DECISION_DATE);
        caseData.put(CcdFields.GENERAL_REFERRAL_REASON, TEST_REASON);
        caseData.put(CcdFields.GENERAL_REFERRAL_TYPE, TEST_TYPE);
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
