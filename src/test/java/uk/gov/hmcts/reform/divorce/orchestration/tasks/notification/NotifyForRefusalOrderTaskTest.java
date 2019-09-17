package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class NotifyForRefusalOrderTaskTest {

    private Map<String, Object> incomingPayload;
    private TaskContext taskContext;

    private String testCaseReference = "testCaseReference";
    private String petitionerEmail = "applicant@divorce.gov.uk";
    private String petitionerFirstName = "Jerry";
    private String petitionerLastName = "Johnson";

    @Mock
    private EmailService emailService;

    @InjectMocks
    NotifyForRefusalOrderTask notifyForRefusalOrderTask;


    @Before
    public void setUp() {
        incomingPayload = new HashMap<>();
        incomingPayload.put(D_8_PETITIONER_EMAIL, petitionerEmail);
        incomingPayload.put(D_8_PETITIONER_FIRST_NAME, petitionerFirstName);
        incomingPayload.put(D_8_PETITIONER_LAST_NAME, petitionerLastName);
        incomingPayload.put(D_8_CASE_REFERENCE, testCaseReference);

        taskContext = new DefaultTaskContext();
    }

    @Test
    public void testNotificationServiceIsCalledWithExpectedParametersWhenRefusalDecisionIsMoreInfo() throws TaskException {
        incomingPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        incomingPayload.put(REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE);
        Map returnedPayload = notifyForRefusalOrderTask.execute(taskContext, incomingPayload);

        assertThat(returnedPayload, equalTo(incomingPayload));
        verify(emailService).sendEmail(eq(petitionerEmail),
            eq(EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION.name()),
            argThat(new HamcrestArgumentMatcher<>(
                allOf(
                    hasEntry(NOTIFICATION_CASE_NUMBER_KEY, testCaseReference),
                    hasEntry(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, petitionerFirstName),
                    hasEntry(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, petitionerLastName)
                )
            )),
            anyString()
        );
    }

    @Test
    public void testNotificationServiceIsNotCalledWithNoRefusalDecision() throws TaskException {
        Map returnedPayload = notifyForRefusalOrderTask.execute(taskContext, incomingPayload);

        assertThat(returnedPayload, equalTo(incomingPayload));
        verify(emailService, never()).sendEmail(eq(petitionerEmail),
            eq(EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION.name()),
            anyMap(),
            anyString()
        );
    }

    @Test
    public void testNotificationServiceIsNotCalledWhenDecreeNisiIsGrantedAndNotRefused() throws TaskException {
        incomingPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, YES_VALUE);
        incomingPayload.put(REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE);

        Map returnedPayload = notifyForRefusalOrderTask.execute(taskContext, incomingPayload);

        assertThat(returnedPayload, equalTo(incomingPayload));
        verify(emailService, never()).sendEmail(eq(petitionerEmail),
            eq(EmailTemplateNames.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION.name()),
            anyMap(),
            anyString()
        );
    }
}