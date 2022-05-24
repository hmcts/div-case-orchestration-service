package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerCoRespondentRespondedNotificationEmail;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoRespondentAosOfflineNotificationTest {
    @Mock
    private SendPetitionerCoRespondentRespondedNotificationEmail petitionerEmailTask;

    @InjectMocks
    private CoRespondentAosOfflineNotification coRespondentAosOfflineNotification;

    private List<Task<Map<String, Object>>> tasks;

    @Before
    public void setUp() {
        tasks = new LinkedList<>();
    }

    @Test
    public void test_addAOSEmailTasks() {
        String caseId = OfflineAosTestFixture.CASE_ID;

        CaseDetails caseDetails = mock(CaseDetails.class);
        when(caseDetails.getCaseId()).thenReturn(caseId);

        coRespondentAosOfflineNotification.addAOSEmailTasks(tasks, caseDetails);

        verify(caseDetails, atLeast(1)).getCaseId();

        assertThat(tasks, hasSize(1));
        assertThat(tasks.get(0), is(petitionerEmailTask));
    }
}
