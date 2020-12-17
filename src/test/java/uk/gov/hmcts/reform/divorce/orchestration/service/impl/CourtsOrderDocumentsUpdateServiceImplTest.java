package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DocumentGenerationWorkflow;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;

@RunWith(MockitoJUnitRunner.class)
public class CourtsOrderDocumentsUpdateServiceImplTest {

    private static final String COE_ENGLISH_TEMPLATE_ID = "FL-DIV-GNO-ENG-00020.docx";//TODO - this is actually being ignored
    private static final String COE_FILE_NAME = "certificateOfEntitlement";//TODO - are file names always the same per document type? if so, maybe this could be inferred from doc type

    @Mock
    private DocumentGenerationWorkflow documentGenerationWorkflow;

    @InjectMocks
    private CourtsOrderDocumentsUpdateServiceImpl classUnderTest;

    private CaseDetails incomingCaseDetails;

    @Before
    public void setUp() {
        Map<String, Object> incomingCaseData = Map.of("coeFile", "existingCoeFile");
        incomingCaseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(incomingCaseData).build();
    }

    @Test
    public void shouldUpdateExistingCourtOrderDocuments() throws WorkflowException, CaseOrchestrationServiceException {
        when(documentGenerationWorkflow.run(incomingCaseDetails, AUTH_TOKEN, COE_ENGLISH_TEMPLATE_ID, DOCUMENT_TYPE_COE, COE_FILE_NAME))
            .thenReturn(Map.of("coeFile", "newCoeFile"));

        Map<String, Object> returnedPayload = classUnderTest.updateExistingCourtOrderDocuments(AUTH_TOKEN, incomingCaseDetails);

        assertThat(returnedPayload.size(), is(incomingCaseDetails.getCaseData().size()));
        assertThat(returnedPayload, hasEntry("coeFile", "newCoeFile"));

        //Certificate of Entitlement
        verify(documentGenerationWorkflow).run(incomingCaseDetails, AUTH_TOKEN, COE_ENGLISH_TEMPLATE_ID, DOCUMENT_TYPE_COE, COE_FILE_NAME);
        //TODO - it would be nicer to just pass the DocumentType. Maybe one for the refactoring stage / week?
        // TODO - does hardcoding the CCD parameters create a risk? I think so. - if something changes in CCD, it won't be reflected here
    }

    @Test
    public void shouldThrowCaseOrchestrationExceptionIfCertificateOfEntitlementGenerationFails() throws WorkflowException {
        when(documentGenerationWorkflow.run(incomingCaseDetails, AUTH_TOKEN, COE_ENGLISH_TEMPLATE_ID, DOCUMENT_TYPE_COE, COE_FILE_NAME))
            .thenThrow(WorkflowException.class);

        CaseOrchestrationServiceException serviceException = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> classUnderTest.updateExistingCourtOrderDocuments(AUTH_TOKEN, incomingCaseDetails)
        );

        assertThat(serviceException.getCaseId().orElseThrow(), is(TEST_CASE_ID));
        assertThat(serviceException.getCause(), isA(WorkflowException.class));
    }

    //TODO - write scenarios where documents don't exist (i.e. have not been created ever)

}