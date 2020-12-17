package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DocumentGenerationWorkflow;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INCOMING_CASE_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;

@RunWith(MockitoJUnitRunner.class)
public class CourtsOrderDocumentsUpdateServiceImplTest {

    @Mock
    private DocumentGenerationWorkflow documentGenerationWorkflow;

    @InjectMocks
    private CourtsOrderDocumentsUpdateServiceImpl classUnderTest;

    @Test
    public void shouldUpdateExistingCourtOrderDocuments() throws WorkflowException {
        classUnderTest.updateExistingCourtOrderDocuments(TEST_INCOMING_CASE_DETAILS, AUTH_TOKEN);

        //COE
        // /generate-document?templateId=FL-DIV-GNO-ENG-00020.docx&documentType=coe&filename=certificateOfEntitlement
//        return //TODO - what do I need to return? how can I chain these returns
        verify(documentGenerationWorkflow).run(TEST_INCOMING_CASE_DETAILS, AUTH_TOKEN,
            "FL-DIV-GNO-ENG-00020.docx",//TODO - this is actually being ignored
            DOCUMENT_TYPE_COE,
            "certificateOfEntitlement"//TODO - are file names always the same per document type? if so, maybe this could be inferred from doc type
        );
        //TODO - it would be nicer to just pass the DocumentType. Maybe one for the refactoring stage / week?
        // TODO - does hardcoding the CCD parameters create a risk? I think so. - if something changes in CCD, it won't be reflected here
    }

    //TODO - write scenarios where documents don't exist (i.e. have not been created ever)

}