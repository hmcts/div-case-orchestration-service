package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PopulateDocLink;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATIONS;
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

        when(populateDocLink.execute(taskContext, caseData)).thenReturn(caseData);

        executeWorkflow(caseData, MINI_PETITION_LINK);

        verify(populateDocLink).execute(taskContext, caseData);
    }

    @Test
    public void runShouldReturnCaseDataAndNotExecuteTasksWhenServiceApplicationIsGrantedAndIsRequestingRespondentAnswers()
        throws Exception {

        Map<String, Object> caseData = buildCaseData(DEEMED, YES_VALUE);
        taskContext.setTransientObject(DOCUMENT_DRAFT_LINK_FIELD, RESP_ANSWERS_LINK);

        executeWorkflow(caseData, RESP_ANSWERS_LINK);

        verify(populateDocLink, times(0)).execute(taskContext, caseData);
    }

    @Test
    public void runShouldReturnCaseDataAndExecuteTasksWhenServiceApplicationIsGrantedAndIsNotRequestingRespondentAnswers()
        throws Exception {

        Map<String, Object> caseData = buildCaseData(DEEMED, YES_VALUE);

        when(populateDocLink.execute(taskContext, caseData)).thenReturn(caseData);

        executeWorkflow(caseData, MINI_PETITION_LINK);

        verify(populateDocLink).execute(taskContext, caseData);
    }

    @Test
    public void runShouldReturnCaseDataAndExecuteTasksWhenServiceApplicationIsNotGrantedAndIsRequestingRespondentAnswers()
        throws Exception {

        Map<String, Object> caseData = buildCaseData(DEEMED, NO_VALUE);
        taskContext.setTransientObject(DOCUMENT_DRAFT_LINK_FIELD, RESP_ANSWERS_LINK);

        when(populateDocLink.execute(taskContext, caseData)).thenReturn(caseData);

        executeWorkflow(caseData, RESP_ANSWERS_LINK);

        verify(populateDocLink).execute(taskContext, caseData);
    }

    @Test
    public void runShouldReturnCaseDataAndExecuteTasksWhenServiceApplicationTypeIsNotDefinedAndIsRequestingRespondentAnswers()
        throws Exception {

        Map<String, Object> caseData = buildCaseData(null, YES_VALUE);
        taskContext.setTransientObject(DOCUMENT_DRAFT_LINK_FIELD, RESP_ANSWERS_LINK);

        when(populateDocLink.execute(taskContext, caseData)).thenReturn(caseData);

        executeWorkflow(caseData, RESP_ANSWERS_LINK);

        verify(populateDocLink).execute(taskContext, caseData);
    }

    public static Map<String, Object> buildCaseData(String type, String granted) {

        CollectionMember<DivorceServiceApplication> application = new CollectionMember<>();
        DivorceServiceApplication.DivorceServiceApplicationBuilder applicationBuilder = DivorceServiceApplication.builder();

        Optional.ofNullable(type).ifPresent(applicationBuilder::type);
        application.setValue(applicationBuilder.applicationGranted(granted).build());

        return ImmutableMap.of(
            SERVICE_APPLICATIONS, asList(application)
        );
    }

    private void executeWorkflow(Map<String, Object> caseData, String respAnswersLink)
        throws WorkflowException {

        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        assertThat(
            solicitorDnFetchDocWorkflow.run(caseDetails, DOCUMENT_TYPE_PETITION, respAnswersLink),
            is(caseData)
        );
    }
}
