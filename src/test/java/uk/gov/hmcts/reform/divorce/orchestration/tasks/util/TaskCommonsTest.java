package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.CourtLookupService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.TaskCommons;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION;

@RunWith(MockitoJUnitRunner.class)
public class TaskCommonsTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private EmailService emailService;

    @Mock
    private CourtLookupService courtLookupService;

    @InjectMocks
    private TaskCommons taskCommons;

    @Test
    public void testTaskExceptionIsThrown_WhenEmailCannotBeSent() throws TaskException, NotificationClientException {
        expectedException.expect(TaskException.class);
        expectedException.expectMessage("Failed to send e-mail");
        HashMap<String, String> templateParameters = new HashMap<>();
        doThrow(NotificationClientException.class)
                .when(emailService)
                .sendEmailAndReturnExceptionIfFails("test@hmcts.net",
                    RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION.name(),
                    templateParameters,
                    "test description");

        taskCommons.sendEmail(RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION,
                "test description",
                "test@hmcts.net",
                templateParameters);
    }

    @Test
    public void testTaskExceptionIsThrown_WhenCourtLookupServiceFails() throws CourtDetailsNotFound, TaskException {
        expectedException.expect(TaskException.class);
        expectedException.expectCause(instanceOf(CourtDetailsNotFound.class));

        doThrow(CourtDetailsNotFound.class)
                .when(courtLookupService)
                .getCourtByKey("testKey");

        taskCommons.getCourt("testKey");
    }

    @Test
    public void testDnCourtIsReturned_WhenDnCourtLookupSucceeds() throws CourtDetailsNotFound {
        DnCourt expectedCourt = new DnCourt();
        doReturn(expectedCourt)
                .when(courtLookupService)
                .getDnCourtByKey("testKey");

        assertEquals(expectedCourt, taskCommons.getDnCourt("testKey"));
    }

    @Test
    public void testCourtDetailsNotFoundIsThrown_WhenDnCourtLookupServiceFails() throws CourtDetailsNotFound {
        expectedException.expect(CourtDetailsNotFound.class);

        doThrow(CourtDetailsNotFound.class)
                .when(courtLookupService)
                .getDnCourtByKey("testKey");

        taskCommons.getDnCourt("testKey");
    }
}