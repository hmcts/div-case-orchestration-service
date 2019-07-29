package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.event.bulk.BulkCaseAcceptedCasesEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_BULK_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDivorceCasesWithinBulkTaskUTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private UpdateDivorceCasesWithinBulkTask updateDivorceCasesWithinBulkTask;

    @Captor
    private ArgumentCaptor<BulkCaseAcceptedCasesEvent> eventCaptor;

    @Test
    public void whenExecuteTask_theEventIsQueued() throws TaskException {
        DefaultTaskContext taskContext = new DefaultTaskContext();

        final BulkCaseAcceptedCasesEvent expectedEvent = new BulkCaseAcceptedCasesEvent(taskContext, CaseDetails.builder()
            .caseId(TEST_BULK_CASE_ID)
            .caseData(DUMMY_CASE_DATA)
            .build());

        taskContext.setTransientObject(CASE_ID_JSON_KEY, TEST_BULK_CASE_ID);
        updateDivorceCasesWithinBulkTask.execute(taskContext, DUMMY_CASE_DATA);

        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        assertThat(eventCaptor.getValue().getCaseDetails(), is(expectedEvent.getCaseDetails()));
    }
}
