package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdEvents.BO_WELSH_GRANT_DN_MAKE_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_ADMIN_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.DN_REFUSED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.WELSH_LA_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features.DN_REFUSAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED_ADMIN_ERROR_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED_REJECT_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_CLARIFICATION_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_REJECTION_ADDITIONAL_INFO;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_REJECTION_ADDITIONAL_INFO_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WELSH_NEXT_EVENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class SetDNDecisionStateTaskTest {

    private static final String DN_ASK_MORE_INFO_OPTION = "moreInfo";

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private TaskContext taskContext;

    @InjectMocks
    private SetDNDecisionStateTask classToTest;

    @Before
    public void setup() {
        when(featureToggleService.isFeatureEnabled(DN_REFUSAL)).thenReturn(true);
    }

    @Test
    public void givenDNGrantedCase_whenSetDnDecisionState_thenReturnAwaitingPronouncementState() {

        Map<String, Object> caseData = ImmutableMap.of(DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE);

        Map<String, Object> returnedPayload = classToTest.execute(taskContext, caseData);

        assertThat(returnedPayload, allOf(
            hasEntry(STATE_CCD_FIELD, AWAITING_PRONOUNCEMENT),
            hasEntry(DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE)
        ));
    }

    @Test
    public void givenNullDnField_whenSetDnDecisionState_thenReturnAwaitingClarificationState() {

        Map<String, Object> caseData = ImmutableMap.of(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);

        Map<String, Object> returnedPayload = classToTest.execute(taskContext, caseData);

        assertThat(returnedPayload, allOf(
            hasEntry(STATE_CCD_FIELD, AWAITING_CLARIFICATION),
            hasEntry(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE)
        ));
    }

    @Test
    public void givenDnRejected_whenSetDnDecisionState_thenReturnDnRefusedState() {
        Map<String, Object> caseData = ImmutableMap.of(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE,
            REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION);

        Map<String, Object> returnedPayload = classToTest.execute(taskContext, caseData);

        assertThat(returnedPayload, allOf(
            hasEntry(STATE_CCD_FIELD, DN_REFUSED),
            hasEntry(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE),
            hasEntry(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION)
        ));
    }

    @Test
    public void givenDnRejected_whenSetDnDecisionState_withAddInfo_andWelshLanguage_thenReturnWelshDnRefusedState() {
        Map<String, Object> caseData = ImmutableMap.of(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE,
            REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION,
            LANGUAGE_PREFERENCE_WELSH, YES_VALUE,
            REFUSAL_REJECTION_ADDITIONAL_INFO, "some additional info");

        Map<String, Object> returnedPayload = classToTest.execute(taskContext, caseData);

        assertThat(returnedPayload, allOf(
            hasEntry(STATE_CCD_FIELD, WELSH_LA_DECISION),
            hasEntry(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE),
            hasEntry(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION),
            hasEntry(WELSH_NEXT_EVENT, BO_WELSH_GRANT_DN_MAKE_DECISION)
        ));
    }

    @Test
    public void givenDnAdditionalClarification_whenSetDnDecisionState_withAddInfo_andWelshLanguage_thenReturnWelshDnRefusedState() {
        Map<String, Object> caseData = ImmutableMap.of(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE,
            REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE,
            LANGUAGE_PREFERENCE_WELSH, YES_VALUE,
            REFUSAL_CLARIFICATION_ADDITIONAL_INFO, "some additional info");

        Map<String, Object> returnedPayload = classToTest.execute(taskContext, caseData);

        assertThat(returnedPayload, allOf(
            hasEntry(STATE_CCD_FIELD, WELSH_LA_DECISION),
            hasEntry(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE),
            hasEntry(REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE),
            hasEntry(WELSH_NEXT_EVENT, BO_WELSH_GRANT_DN_MAKE_DECISION)
        ));
    }

    @Test
    public void givenDnRejected_whenSetDnDecisionState_withWelshAddInfo_andWelshLanguage_thenReturnDnRefusedState() {
        Map<String, Object> caseData = ImmutableMap.of(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE,
            REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION,
            LANGUAGE_PREFERENCE_WELSH, YES_VALUE, REFUSAL_REJECTION_ADDITIONAL_INFO,
            "some additional info", REFUSAL_REJECTION_ADDITIONAL_INFO_WELSH, "some welsh additional info");

        Map<String, Object> returnedPayload = classToTest.execute(taskContext, caseData);

        assertThat(returnedPayload, allOf(
            hasEntry(STATE_CCD_FIELD, DN_REFUSED),
            hasEntry(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE),
            hasEntry(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION)
        ));
    }

    @Test
    public void givenDnAdminError_whenSetDnDecisionState_thenReturnDnAdminClarificationState() {
        Map<String, Object> caseData = ImmutableMap.of(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE,
            REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_ADMIN_ERROR_OPTION);

        Map<String, Object> returnedPayload = classToTest.execute(taskContext, caseData);

        assertThat(returnedPayload, allOf(
            hasEntry(STATE_CCD_FIELD, AWAITING_ADMIN_CLARIFICATION),
            hasEntry(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE),
            hasEntry(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_ADMIN_ERROR_OPTION)
        ));
    }

    @Test
    public void givenDnAskMoreInfo_whenSetDnDecisionState_thenReturnDnAwaitingClarificationState() {
        Map<String, Object> caseData = ImmutableMap.of(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE,
            REFUSAL_DECISION_CCD_FIELD, DN_ASK_MORE_INFO_OPTION);

        Map<String, Object> returnedPayload = classToTest.execute(taskContext, caseData);

        assertThat(returnedPayload, allOf(
            hasEntry(STATE_CCD_FIELD, AWAITING_CLARIFICATION),
            hasEntry(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE),
            hasEntry(REFUSAL_DECISION_CCD_FIELD, DN_ASK_MORE_INFO_OPTION)
        ));
    }

    @Test
    public void givenDnRejected_And_toggleDisabled_whenSetDnDecisionState_thenReturnDnAwaitingClarificationState() {
        when(featureToggleService.isFeatureEnabled(DN_REFUSAL)).thenReturn(false);

        Map<String, Object> caseData = ImmutableMap.of(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE,
            REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION);

        Map<String, Object> returnedPayload = classToTest.execute(taskContext, caseData);

        assertThat(returnedPayload, allOf(
            hasEntry(STATE_CCD_FIELD, AWAITING_CLARIFICATION),
            hasEntry(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE),
            hasEntry(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION)
        ));
    }
}