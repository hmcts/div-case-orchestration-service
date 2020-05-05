package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDaGrantedDetailsTask;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class DecreeAbsoluteGrantedWorkflowTest {

    @Mock
    private SetDaGrantedDetailsTask setDaGrantedDetailsTask;

    @Mock
    private DocumentGenerationTask documentGenerationTask;

    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @InjectMocks
    private DecreeAbsoluteGrantedWorkflow decreeAbsoluteGrantedWorkflow;

    @Test
    public void testProcessIsNotCalled() throws WorkflowException, TaskException {

        final Map<String, Object> payload = new HashMap<>(ImmutableMap.of(RESP_IS_USING_DIGITAL_CHANNEL, YES_VALUE));

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(payload)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        final Map<String, Object> result = decreeAbsoluteGrantedWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        assertThat(payload, is(result));

        verify(setDaGrantedDetailsTask, never()).execute(any(TaskContext.class), eq(payload));
    }

    @Test
    public void testProcessIsCalled_When_Responded_Is_Not_Using_Digital_Channel() throws WorkflowException, TaskException { //TODO Code review

        final Map<String, Object> expectedResponse = new HashMap<>(
            ImmutableMap.of(
                RESP_IS_USING_DIGITAL_CHANNEL, YES_VALUE,
                PRONOUNCEMENT_JUDGE_CCD_FIELD, "judge"
            )
        );

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(expectedResponse)
            .build();

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        final Task[] tasks = new Task[] {
            setDaGrantedDetailsTask,
            documentGenerationTask,
            caseFormatterAddDocuments
        };
        when(setDaGrantedDetailsTask.execute(any(TaskContext.class), eq(expectedResponse))).thenReturn(expectedResponse);
        when(documentGenerationTask.execute(any(TaskContext.class), eq(expectedResponse))).thenReturn(expectedResponse);
        when(caseFormatterAddDocuments.execute(any(TaskContext.class), eq(expectedResponse))).thenReturn(expectedResponse);

        when(decreeAbsoluteGrantedWorkflow.execute(tasks,
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId()),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(DOCUMENT_TYPE, DECREE_ABSOLUTE_DOCUMENT_TYPE),
            ImmutablePair.of(DOCUMENT_TEMPLATE_ID, DECREE_ABSOLUTE_TEMPLATE_ID),
            ImmutablePair.of(DOCUMENT_FILENAME, DECREE_ABSOLUTE_FILENAME)))
            .thenReturn(expectedResponse);

        Map<String, Object> actual = decreeAbsoluteGrantedWorkflow.run(ccdCallbackRequest, AUTH_TOKEN);

        assertThat(expectedResponse, is(actual));
    }
}