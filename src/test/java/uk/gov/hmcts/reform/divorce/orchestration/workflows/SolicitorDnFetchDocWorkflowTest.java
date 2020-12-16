package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PopulateDocLink;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_DRAFT_LINK_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_LINK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ANSWERS_LINK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DEEMED;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorDnFetchDocWorkflowTest {

    @Mock
    PopulateDocLink populateDocLink;

    @InjectMocks
    SolicitorDnFetchDocWorkflow solicitorDnFetchDocWorkflow;

    private TaskContext taskContext;

    @Before
    public void setup() {
        taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(DOCUMENT_TYPE, DOCUMENT_TYPE_PETITION);
        taskContext.setTransientObject(DOCUMENT_DRAFT_LINK_FIELD, MINI_PETITION_LINK);
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {

        Map<String, Object> caseData = Collections.emptyMap();

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .build();

        when(populateDocLink.execute(taskContext, caseData)).thenReturn(caseData);

        assertThat(solicitorDnFetchDocWorkflow.run(caseDetails, DOCUMENT_TYPE_PETITION, MINI_PETITION_LINK), is(caseData));

        verify(populateDocLink).execute(taskContext, caseData);
    }

    @Test
    public void runShouldReturnCaseDataAndNotExecuteTasksWhenServiceApplicationIsGrantedAndIsRequestingRespondentAnswers() throws Exception {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SERVICE_APPLICATION_TYPE, DEEMED);
        caseData.put(SERVICE_APPLICATION_GRANTED, YES_VALUE);
        taskContext.setTransientObject(DOCUMENT_DRAFT_LINK_FIELD, RESP_ANSWERS_LINK);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        assertThat(solicitorDnFetchDocWorkflow.run(caseDetails, DOCUMENT_TYPE_PETITION, RESP_ANSWERS_LINK), is(caseData));

        verify(populateDocLink, times(0)).execute(taskContext, caseData);
    }

    @Test
    public void runShouldReturnCaseDataAndExecuteTasksWhenServiceApplicationIsGrantedAndIsNotRequestingRespondentAnswers() throws Exception {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SERVICE_APPLICATION_TYPE, DEEMED);
        caseData.put(SERVICE_APPLICATION_GRANTED, YES_VALUE);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        when(populateDocLink.execute(taskContext, caseData)).thenReturn(caseData);

        assertThat(solicitorDnFetchDocWorkflow.run(caseDetails, DOCUMENT_TYPE_PETITION, MINI_PETITION_LINK), is(caseData));

        verify(populateDocLink).execute(taskContext, caseData);
    }

    @Test
    public void runShouldReturnCaseDataAndExecuteTasksWhenServiceApplicationIsNotGrantedAndIsRequestingRespondentAnswers() throws Exception {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SERVICE_APPLICATION_TYPE, DEEMED);
        caseData.put(SERVICE_APPLICATION_GRANTED, NO_VALUE);
        taskContext.setTransientObject(DOCUMENT_DRAFT_LINK_FIELD, RESP_ANSWERS_LINK);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        when(populateDocLink.execute(taskContext, caseData)).thenReturn(caseData);

        assertThat(solicitorDnFetchDocWorkflow.run(caseDetails, DOCUMENT_TYPE_PETITION, RESP_ANSWERS_LINK), is(caseData));

        verify(populateDocLink).execute(taskContext, caseData);
    }

    @Test
    public void runShouldReturnCaseDataAndExecuteTasksWhenServiceApplicationTypeIsNotDefinedAndIsRequestingRespondentAnswers() throws Exception {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SERVICE_APPLICATION_GRANTED, YES_VALUE);
        taskContext.setTransientObject(DOCUMENT_DRAFT_LINK_FIELD, RESP_ANSWERS_LINK);

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        when(populateDocLink.execute(taskContext, caseData)).thenReturn(caseData);

        assertThat(solicitorDnFetchDocWorkflow.run(caseDetails, DOCUMENT_TYPE_PETITION, RESP_ANSWERS_LINK), is(caseData));

        verify(populateDocLink).execute(taskContext, caseData);
    }
}