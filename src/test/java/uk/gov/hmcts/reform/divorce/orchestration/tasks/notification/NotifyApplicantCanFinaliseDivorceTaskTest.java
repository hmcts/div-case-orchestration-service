package uk.gov.hmcts.reform.divorce.orchestration.tasks.notification;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.TaskCommons;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;

@RunWith(MockitoJUnitRunner.class)
public class NotifyApplicantCanFinaliseDivorceTaskTest {

    private static final String EXPECTED_EMAIL_DEFINITION = "Applicant can finalise divorce";

    private static final String CASE_NUMBER_TEMPLATE_KEY = "case number";
    private static final String FIRST_NAME_TEMPLATE_KEY = "first name";
    private static final String LAST_NAME_TEMPLATE_KEY = "last name";

    private Map<String, Object> incomingPayload;
    private TaskContext taskContext;

    private String testCaseId = "testCaseId";

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private TaskCommons taskCommons;

    @InjectMocks
    NotifyApplicantCanFinaliseDivorceTask notifyApplicantCanFinaliseDivorceTask;


    @Before
    public void setUp() {
        incomingPayload = new HashMap<>();
        incomingPayload.put(D_8_PETITIONER_EMAIL, "applicant@divorce.gov.uk");
        incomingPayload.put(D_8_PETITIONER_FIRST_NAME, "Jerry");
        incomingPayload.put(D_8_PETITIONER_LAST_NAME, "Johnson");
        incomingPayload.put(D_8_CASE_REFERENCE, "testFamilyManReferenceNumber");

        taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(CASE_ID_JSON_KEY, testCaseId);
    }

    @Test
    public void testNotificationServiceIsCalledWithExpectedParameters() throws TaskException {
        Map returnedPayload = notifyApplicantCanFinaliseDivorceTask.execute(taskContext, incomingPayload);

        assertThat(returnedPayload, equalTo(incomingPayload));
        verify(taskCommons).sendEmail(eq(EmailTemplateNames.CASE_ELIGIBLE_FOR_NOTIFICATION_FOR_APPLICANT),
            eq(EXPECTED_EMAIL_DEFINITION),
            eq("applicant@divorce.gov.uk"),
            argThat(new HamcrestArgumentMatcher<>(
                allOf(
                    hasEntry(CASE_NUMBER_TEMPLATE_KEY, "testFamilyManReferenceNumber"),
                    hasEntry(FIRST_NAME_TEMPLATE_KEY, "Jerry"),
                    hasEntry(LAST_NAME_TEMPLATE_KEY, "Johnson")
                )
            )));
    }

    @Test
    public void testNewExceptionIsThrown_WhenNotificationServiceThrowsException() throws TaskException {
        doThrow(TaskException.class).when(taskCommons).sendEmail(any(), any(), any(), any());
        expectedException.expect(TaskException.class);

        notifyApplicantCanFinaliseDivorceTask.execute(taskContext, incomingPayload);
    }

}