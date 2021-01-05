package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CancelPronouncementDetailsWithinBulkTask;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.DUMMY_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.class)
public class BulkCaseCancelPronouncementEventWorkflowTest {

    @Mock
    private CancelPronouncementDetailsWithinBulkTask cancelPronouncementDetailsWithinBulkTask;

    @InjectMocks
    private BulkCaseCancelPronouncementEventWorkflow classToTest;

    @Test
    public void whenProcessAwaitingPronouncement_thenProcessAsExpected() throws Exception {
        CcdCallbackRequest callbackRequest = CcdCallbackRequest.builder()
            .caseDetails(CaseDetails
                .builder()
                .caseId(TEST_CASE_ID)
                .caseData(DUMMY_CASE_DATA)
                .build())
            .build();

        Map<String, Object> expected = Collections.emptyMap();

        when(cancelPronouncementDetailsWithinBulkTask.execute(any(TaskContext.class), eq(DUMMY_CASE_DATA))).thenReturn(expected);

        Map<String, Object> actual = classToTest.run(callbackRequest, AUTH_TOKEN);

        assertEquals(expected, actual);
    }
}
