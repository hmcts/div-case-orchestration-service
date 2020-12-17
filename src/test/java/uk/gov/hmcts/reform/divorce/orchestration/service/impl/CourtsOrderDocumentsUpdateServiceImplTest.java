package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.service.CourtsOrderDocumentsUpdateService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DocumentGenerationWorkflow;

@RunWith(MockitoJUnitRunner.class)
public class CourtsOrderDocumentsUpdateServiceImplTest {

    @Mock
    private DocumentGenerationWorkflow documentGenerationWorkflow;

    @InjectMocks
    private CourtsOrderDocumentsUpdateServiceImpl classUnderTest;

    @Test
    public void shouldUpdateExistingCourtOrderDocuments() {
        classUnderTest.updateExistingCourtOrderDocuments();

        //COE
        // /generate-document?templateId=FL-DIV-GNO-ENG-00020.docx&documentType=coe&filename=certificateOfEntitlement
//        return //TODO - what do I need to return? how can I chain these returns
//        verify(documentGenerationWorkflow).run(ccdCallbackRequest, authToken, templateId, documentType, filename);//TODO - come back to this after refactor
// TODO - does hardcoding the CCD parameters create a risk? I think so.
    }

    //TODO - write scenarios where documents don't exist

}