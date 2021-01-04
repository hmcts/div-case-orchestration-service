package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CourtOrderDocumentsUpdateService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DocumentGenerationWorkflow;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INCOMING_CASE_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocument;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getObjectMapperInstance;

@RunWith(MockitoJUnitRunner.class)
public class CourtOrderDocumentsUpdateServiceImplTest {

    private static final String COE_ENGLISH_TEMPLATE_ID = "FL-DIV-GNO-ENG-00020.docx";
    private static final String COE_FILE_NAME = "certificateOfEntitlement";

    @Mock
    private DocumentGenerationWorkflow documentGenerationWorkflow;

    private CourtOrderDocumentsUpdateService classUnderTest;

    private CaseDetails incomingCaseDetails;

    @Before
    public void setUp() {
        classUnderTest = new CourtOrderDocumentsUpdateServiceImpl(getObjectMapperInstance(), documentGenerationWorkflow);

        Map<String, Object> incomingCaseData = Map.of(D8DOCUMENTS_GENERATED, List.of(
            createCollectionMemberDocument("oldUrl", DOCUMENT_TYPE_COE, COE_FILE_NAME)
        ));
        incomingCaseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(incomingCaseData).build();
    }

    @Test
    public void shouldUpdateExistingCourtOrderDocuments() throws WorkflowException, CaseOrchestrationServiceException {
        when(documentGenerationWorkflow.run(incomingCaseDetails, AUTH_TOKEN, COE_ENGLISH_TEMPLATE_ID, DOCUMENT_TYPE_COE, COE_FILE_NAME))
            .thenReturn(
                Map.of(D8DOCUMENTS_GENERATED, List.of(
                    createCollectionMemberDocument("newUrl", DOCUMENT_TYPE_COE, COE_FILE_NAME)
                ))
            );

        Map<String, Object> returnedPayload = classUnderTest.updateExistingCourtOrderDocuments(AUTH_TOKEN, incomingCaseDetails);

        List<CollectionMember<Document>> generatedDocuments = (List<CollectionMember<Document>>) returnedPayload.get(D8DOCUMENTS_GENERATED);
        assertThat(generatedDocuments.size(), is(incomingCaseDetails.getCaseData().size()));
        CollectionMember<Document> updatedCertificateOfEntitlement = generatedDocuments.get(0);
        assertThat(updatedCertificateOfEntitlement.getValue().getDocumentType(), is(DOCUMENT_TYPE_COE));
        assertThat(updatedCertificateOfEntitlement.getValue().getDocumentLink().getDocumentUrl(), is("newUrl"));

        //Certificate of Entitlement
        verify(documentGenerationWorkflow).run(incomingCaseDetails, AUTH_TOKEN, COE_ENGLISH_TEMPLATE_ID, DOCUMENT_TYPE_COE, COE_FILE_NAME);
    }

    @Test
    public void shouldNotUpdateCourtOrderDocumentsIfTheyDoNotAlreadyExist() throws CaseOrchestrationServiceException, WorkflowException {
        Map<String, Object> returnedPayload = classUnderTest.updateExistingCourtOrderDocuments(AUTH_TOKEN, TEST_INCOMING_CASE_DETAILS);

        assertThat(returnedPayload, equalTo(TEST_INCOMING_CASE_DETAILS.getCaseData()));

        verify(documentGenerationWorkflow, never()).run(any(), any(), any(), any(), any());
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

}