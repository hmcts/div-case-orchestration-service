package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDaGrantedDetailsTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class DecreeAbsoluteAboutToBeGrantedWorkflowTest {

    @Mock
    private SetDaGrantedDetailsTask setDaGrantedDetailsTask;

    @Mock
    private DocumentGenerationTask documentGenerationTask;

    @Mock
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Mock
    private DocumentTemplateService documentTemplateService;

    @InjectMocks
    private DecreeAbsoluteAboutToBeGrantedWorkflow decreeAbsoluteAboutToBeGrantedWorkflow;

    private static final String DECREE_ABSOLUTE_TEMPLATE_ID = "FL-DIV-GOR-ENG-00062.docx";


    @Test
    public void callsTheRequiredTasksInOrder() throws WorkflowException, TaskException {
        final TaskContext context = new DefaultTaskContext();
        final Map<String, Object> payload = new HashMap<>();

        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(payload)
            .build();

        when(documentTemplateService.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.DECREE_ABSOLUTE_TEMPLATE_ID))
                .thenReturn(DECREE_ABSOLUTE_TEMPLATE_ID);

        final CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();
        final Map<String, Object> result = decreeAbsoluteAboutToBeGrantedWorkflow.run(ccdCallbackRequest, "auth");

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
        context.setTransientObject(CASE_ID_JSON_KEY, caseDetails.getCaseId());
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, "auth");
        context.setTransientObject(DOCUMENT_TEMPLATE_ID, DECREE_ABSOLUTE_TEMPLATE_ID);
        context.setTransientObject(DOCUMENT_TYPE, DECREE_ABSOLUTE_DOCUMENT_TYPE);
        context.setTransientObject(DOCUMENT_FILENAME, DECREE_ABSOLUTE_FILENAME);

        assertThat(result, is(payload));

        final InOrder inOrder = inOrder(
                setDaGrantedDetailsTask,
                documentGenerationTask,
                caseFormatterAddDocuments);

        inOrder.verify(setDaGrantedDetailsTask).execute(context, payload);
        inOrder.verify(documentGenerationTask).execute(context, payload);
        inOrder.verify(caseFormatterAddDocuments).execute(context, payload);
    }

}
