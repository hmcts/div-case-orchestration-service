package uk.gov.hmcts.reform.divorce.orchestration.workflows.notification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaGrantedNotificationEmail;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SendDaGrantedNotificationEmailWorkflowTest {

    @Mock
    private SendDaGrantedNotificationEmail sendDaGrantedNotificationEmail;

    @InjectMocks
    private SendDaGrantedNotificationEmailWorkflow sendDaGrantedNotificationEmailWorkflow;

    @Test
    public void notifyApplicantCanFinaliseDivorceTaskIsExecuted() throws Exception {
        Map<String, Object> casePayload = Collections.emptyMap();

        when(sendDaGrantedNotificationEmail.execute(isNotNull(), eq(casePayload))).thenReturn(casePayload);

        sendDaGrantedNotificationEmailWorkflow.run(casePayload);

        verify(sendDaGrantedNotificationEmail).execute(any(TaskContext.class), eq(casePayload));
    }
}