package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.CourtLookupService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.TaskCommons;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION;

@RunWith(MockitoJUnitRunner.class)
public class TaskCommonsTest {
    @Mock
    private EmailService emailService;

    @Mock
    private CourtLookupService courtLookupService;

    @InjectMocks
    private TaskCommons taskCommons;

    @Test
    public void testTaskExceptionIsThrown_WhenEmailCannotBeSent() throws TaskException, NotificationClientException {
        HashMap<String, String> templateParameters = new HashMap<>();
        doThrow(NotificationClientException.class)
            .when(emailService)
            .sendEmailAndReturnExceptionIfFails("test@hmcts.net",
                RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION.name(),
                templateParameters,
                "test description",
                LanguagePreference.ENGLISH);

        TaskException taskException = assertThrows(
            TaskException.class,
            () -> taskCommons.sendEmail(
                RESPONDENT_DEFENDED_AOS_SUBMISSION_NOTIFICATION,
                "test description",
                "test@hmcts.net",
                templateParameters,
                LanguagePreference.ENGLISH
            )
        );

        assertThat(taskException.getMessage(), is("Failed to send e-mail"));
    }

    @Test
    public void testTaskExceptionIsThrown_WhenCourtLookupServiceFails() throws CourtDetailsNotFound, TaskException {
        doThrow(CourtDetailsNotFound.class)
            .when(courtLookupService)
            .getCourtByKey("testKey");

        TaskException taskException = assertThrows(
            TaskException.class,
            () -> taskCommons.getCourt("testKey")
        );

        assertThat(taskException.getCause(), is(instanceOf(CourtDetailsNotFound.class)));
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
        doThrow(CourtDetailsNotFound.class)
            .when(courtLookupService)
            .getDnCourtByKey("testKey");

        assertThrows(
            CourtDetailsNotFound.class,
            () -> taskCommons.getDnCourt("testKey")
        );
    }
}
