package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetFormattedDnCourtDetails;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDivorceCaseHearingDetailsWithinBulk;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class BulkCaseUpdateHearingDetailsEventWorkflowTest {

    @Mock
    private UpdateDivorceCaseHearingDetailsWithinBulk updateDivorceCaseHearingDetailsWithinBulk;

    @Mock
    private SetFormattedDnCourtDetails setFormattedDnCourtDetails;

    @Mock
    private DocumentGenerationTask documentGenerationTask;

    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @InjectMocks
    private BulkCaseUpdateHearingDetailsEventWorkflow classUnderTest;

    @Test
    public void whenProcessAwaitingPronouncement_thenProcessAsExpected() throws WorkflowException, TaskException {

        Map<String, Object> expected = emptyMap();

        when(updateDivorceCaseHearingDetailsWithinBulk.execute(any(TaskContext.class), eq(emptyMap()))).thenReturn(expected);

        Map<String, Object> actual = classUnderTest.run(CcdCallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder()
                .caseData(emptyMap()).build())
            .build(), AUTH_TOKEN);

        assertEquals(expected, actual);
    }

    @Test
    public void givenJudgeName_whenProcessAwaitingPronouncement_thenGeneratedPronouncementDocument() throws Exception {

        Map<String, Object> expected = ImmutableMap.of(PRONOUNCEMENT_JUDGE_CCD_FIELD, "JudgeName");

        when(setFormattedDnCourtDetails.execute(any(TaskContext.class), eq(expected))).thenReturn(expected);
        when(documentGenerationTask.execute(any(TaskContext.class), eq(expected))).thenReturn(expected);
        when(caseFormatterAddDocuments.execute(any(TaskContext.class), eq(expected))).thenReturn(expected);
        when(updateDivorceCaseHearingDetailsWithinBulk.execute(any(TaskContext.class), eq(expected))).thenReturn(expected);

        Map<String, Object> actual = classUnderTest.run(CcdCallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder()
                .caseData(
                    expected
                ).build())
            .build(), AUTH_TOKEN);

        assertEquals(expected, actual);

        final InOrder inOrder = inOrder(
            setFormattedDnCourtDetails,
            documentGenerationTask,
            caseFormatterAddDocuments,
            updateDivorceCaseHearingDetailsWithinBulk
        );

        inOrder.verify(setFormattedDnCourtDetails).execute(any(TaskContext.class), eq(expected));
        inOrder.verify(documentGenerationTask).execute(any(TaskContext.class), eq(expected));
        inOrder.verify(caseFormatterAddDocuments).execute(any(TaskContext.class), eq(expected));
        inOrder.verify(updateDivorceCaseHearingDetailsWithinBulk).execute(any(TaskContext.class), eq(expected));
    }
}
