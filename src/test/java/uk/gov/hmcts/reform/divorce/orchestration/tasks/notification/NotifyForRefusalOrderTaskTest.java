package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED_REJECT_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_INFERRED_PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_FEES_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_HUSBAND_OR_WIFE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITIONER_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PETITION_FEE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_SOLICITOR_NAME;

@RunWith(MockitoJUnitRunner.class)
public class NotifyForRefusalOrderTaskTest {

    private static final String MALE_GENDER = "male";
    private static final String TEST_CASE_REFERENCE = "TEST_CASE_REFERENCE";
    private static final String PETITIONER_EMAIL = "applicant@divorce.gov.uk";
    private static final String PETITIONER_FIRST_NAME = "Jerry";
    private static final String PETITIONER_LAST_NAME = "Johnson";
    private static final String RELATION = "husband";
    private static final FeeResponse TEST_FEES = FeeResponse.builder().amount(50.00).build();
    private static final String FEE_AMOUNT_AS_STRING = "50";

    private Map<String, Object> incomingPayload;
    private TaskContext taskContext;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotifyForRefusalOrderTask notifyForRefusalOrderTask;

    @Before
    public void setUp() {
        incomingPayload = new HashMap<>();
        incomingPayload.put(D_8_PETITIONER_EMAIL, PETITIONER_EMAIL);
        incomingPayload.put(D_8_PETITIONER_FIRST_NAME, PETITIONER_FIRST_NAME);
        incomingPayload.put(D_8_PETITIONER_LAST_NAME, PETITIONER_LAST_NAME);
        incomingPayload.put(D_8_CASE_REFERENCE, TEST_CASE_REFERENCE);
        incomingPayload.put(D_8_INFERRED_PETITIONER_GENDER, MALE_GENDER);
        taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(PETITION_FEE_JSON_KEY, TEST_FEES);
        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
    }

    @Test
    public void notificationServiceIsCalledWithExpectedParametersWhenRefusalDecisionIsMoreInfo() throws TaskException {
        incomingPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        incomingPayload.put(REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE);

        executeTask();

        verify(emailService).sendEmail(
            eq(PETITIONER_EMAIL),
            eq(EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION.name()),
            argThat(new HamcrestArgumentMatcher<>(
                allOf(
                    hasEntry(NOTIFICATION_CASE_NUMBER_KEY, TEST_CASE_REFERENCE),
                    hasEntry(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, PETITIONER_FIRST_NAME),
                    hasEntry(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, PETITIONER_LAST_NAME)
                )
            )),
            anyString()
        );
    }

    @Test
    public void notificationServiceForSolicitorIsCalledWhenRefusalDecisionIsMoreInfo() throws TaskException {
        incomingPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        incomingPayload.put(REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE);
        incomingPayload.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        incomingPayload.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        incomingPayload.put(RESP_FIRST_NAME_CCD_FIELD, TEST_USER_FIRST_NAME);
        incomingPayload.put(RESP_LAST_NAME_CCD_FIELD, TEST_USER_LAST_NAME);

        executeTask();

        verify(emailService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(EmailTemplateNames.SOL_DN_DECISION_MADE.name()),
            argThat(new HamcrestArgumentMatcher<>(
                allOf(
                    hasEntry(NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID),
                    hasEntry(NOTIFICATION_PET_NAME, PETITIONER_FIRST_NAME + " " + PETITIONER_LAST_NAME),
                    hasEntry(NOTIFICATION_RESP_NAME, TEST_USER_FIRST_NAME + " " + TEST_USER_LAST_NAME),
                    hasEntry(NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME)
                )
            )),
            anyString()
        );
    }

    @Test
    public void notificationServiceIsCalledWithExpectedParametersWhenRefusalDecisionIsRejection() throws TaskException {
        incomingPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        incomingPayload.put(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION);

        executeTask();

        verify(emailService).sendEmail(
            eq(PETITIONER_EMAIL),
            eq(EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_REJECTION.name()),
            argThat(new HamcrestArgumentMatcher<>(
                allOf(
                    hasEntry(NOTIFICATION_CASE_NUMBER_KEY, TEST_CASE_REFERENCE),
                    hasEntry(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, PETITIONER_FIRST_NAME),
                    hasEntry(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, PETITIONER_LAST_NAME),
                    hasEntry(NOTIFICATION_HUSBAND_OR_WIFE, RELATION),
                    hasEntry(NOTIFICATION_FEES_KEY, FEE_AMOUNT_AS_STRING)
                )
            )),
            anyString()
        );
    }

    @Test
    public void notificationServiceIsCalledWithExpectedParametersWhenRefusalDecisionIsRejection_forSolicitor() throws TaskException {
        incomingPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        incomingPayload.put(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION);
        incomingPayload.put(PETITIONER_SOLICITOR_EMAIL, TEST_SOLICITOR_EMAIL);
        incomingPayload.put(PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        incomingPayload.put(RESP_FIRST_NAME_CCD_FIELD, TEST_USER_FIRST_NAME);
        incomingPayload.put(RESP_LAST_NAME_CCD_FIELD, TEST_USER_LAST_NAME);
        incomingPayload.put(PETITIONER_EMAIL, null);

        executeTask();

        verify(emailService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_REJECTION_SOLICITOR.name()),
            argThat(new HamcrestArgumentMatcher<>(
                allOf(
                    hasEntry(NOTIFICATION_CCD_REFERENCE_KEY, TEST_CASE_ID),
                    hasEntry(NOTIFICATION_PET_NAME, PETITIONER_FIRST_NAME + " " + PETITIONER_LAST_NAME),
                    hasEntry(NOTIFICATION_RESP_NAME, TEST_USER_FIRST_NAME + " " + TEST_USER_LAST_NAME),
                    hasEntry(NOTIFICATION_SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                    hasEntry(NOTIFICATION_FEES_KEY, FEE_AMOUNT_AS_STRING)
                )
            )),
            anyString()
        );
    }

    @Test
    public void notificationServiceIsNotCalledWithNoRefusalDecision() throws TaskException {
        executeTask();

        verifyEmailNeverSent(PETITIONER_EMAIL, EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION);
    }

    @Test
    public void notificationServiceIsNotCalledWhenDecreeNisiIsGrantedAndNotRefused() throws TaskException {
        incomingPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE);
        incomingPayload.put(REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE);

        executeTask();

        verifyEmailNeverSent(PETITIONER_EMAIL, EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION);
    }

    @Test
    public void notificationServiceIsNotCalledWhenNoPetitioner() throws TaskException {
        incomingPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        incomingPayload.put(REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE);
        incomingPayload.remove(D_8_PETITIONER_EMAIL);

        executeTask();

        verifyEmailNeverSent(PETITIONER_EMAIL, EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION);
    }

    private void executeTask() throws TaskException {
        Map<String, Object> returnedPayload = notifyForRefusalOrderTask.execute(taskContext, incomingPayload);

        assertThat(returnedPayload, equalTo(incomingPayload));
    }

    private void verifyEmailNeverSent(String email, EmailTemplateNames templateId) {
        verify(emailService, never()).sendEmail(
            eq(email),
            eq(templateId.name()),
            anyMap(),
            anyString()
        );
    }
}
