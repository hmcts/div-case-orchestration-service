package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.TaskCommons;
import uk.gov.service.notify.NotificationClientException;

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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_FEE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COURT_NAME_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED_REJECT_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.EMAIL_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_ADDRESSEE_LAST_NAME_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class PetitionerClarificationSubmittedNotificationTest {

    private static final String TEST_CASE_REFERENCE = "TEST_CASE_REFERENCE";
    private static final String PETITIONER_EMAIL = "applicant@divorce.gov.uk";
    private static final String PETITIONER_FIRST_NAME = "Jerry";
    private static final String PETITIONER_LAST_NAME = "Johnson";
    private static final String COURT_NAME = CourtEnum.EASTMIDLANDS.getId();

    private Map<String, Object> incomingPayload;
    private TaskContext taskContext;
    private Court court;
    @Mock
    private EmailService emailService;

    @Mock
    private TaskCommons taskCommons;

    @InjectMocks
    private PetitionerClarificationSubmittedNotificationEmailTask classToTest;

    @Before
    public void setUp() {
        incomingPayload = new HashMap<>();
        incomingPayload.put(D_8_PETITIONER_EMAIL, PETITIONER_EMAIL);
        incomingPayload.put(D_8_PETITIONER_FIRST_NAME, PETITIONER_FIRST_NAME);
        incomingPayload.put(D_8_PETITIONER_LAST_NAME, PETITIONER_LAST_NAME);
        incomingPayload.put(D_8_CASE_REFERENCE, TEST_CASE_REFERENCE);
        incomingPayload.put(COURT_NAME_CCD_FIELD, COURT_NAME);

        taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AMEND_PETITION_FEE_JSON_KEY, COURT_NAME);
        court = new Court();
        court.setDivorceCentreName(CourtEnum.EASTMIDLANDS.getDisplayName());
    }

    @Test
    public void testNotificationServiceIsCalledWithExpectedParameters() throws TaskException, NotificationClientException {
        when(taskCommons.getCourt(COURT_NAME)).thenReturn(court);

        incomingPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        incomingPayload.put(REFUSAL_DECISION_CCD_FIELD, REFUSAL_DECISION_MORE_INFO_VALUE);
        Map<String, Object> returnedPayload = classToTest.execute(taskContext, incomingPayload);

        assertThat(returnedPayload, equalTo(incomingPayload));
        verify(emailService).sendEmailAndReturnExceptionIfFails(eq(PETITIONER_EMAIL),
            eq(EmailTemplateNames.DECREE_NISI_CLARIFICATION_SUBMISSION.name()),
            argThat(new HamcrestArgumentMatcher<>(
                allOf(
                    hasEntry(NOTIFICATION_REFERENCE_KEY, TEST_CASE_REFERENCE),
                    hasEntry(NOTIFICATION_ADDRESSEE_FIRST_NAME_KEY, PETITIONER_FIRST_NAME),
                    hasEntry(NOTIFICATION_ADDRESSEE_LAST_NAME_KEY, PETITIONER_LAST_NAME),
                    hasEntry(COURT_NAME_TEMPLATE_ID, court.getDivorceCentreName())
                )
            )),
            anyString()
        );
    }

    @Test
    public void testNotificationServiceErrorThenErrorIsInContext() throws Exception {
        incomingPayload.put(DECREE_NISI_GRANTED_CCD_FIELD, NO_VALUE);
        incomingPayload.put(REFUSAL_DECISION_CCD_FIELD, DN_REFUSED_REJECT_OPTION);

        when(taskCommons.getCourt(COURT_NAME)).thenReturn(court);

        doThrow(new NotificationClientException("Notification Error"))
            .when(emailService).sendEmailAndReturnExceptionIfFails(eq(PETITIONER_EMAIL),
                eq(EmailTemplateNames.DECREE_NISI_CLARIFICATION_SUBMISSION.name()),
                anyMap(), anyString());

        classToTest.execute(taskContext, incomingPayload);

        assertThat(taskContext.getTransientObject(EMAIL_ERROR_KEY), equalTo("Notification Error"));
    }
}