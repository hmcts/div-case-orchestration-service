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
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_LINK;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOL_DOCUMENT_LINK_FIELD;

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
        taskContext.setTransientObject(SOL_DOCUMENT_LINK_FIELD, MINI_PETITION_LINK);
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
}