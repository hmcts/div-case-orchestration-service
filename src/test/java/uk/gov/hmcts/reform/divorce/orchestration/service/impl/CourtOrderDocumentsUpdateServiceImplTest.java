package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CourtOrderDocumentsUpdateService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DocumentGenerationWorkflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INCOMING_CASE_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CERTIFICATE_OF_ENTITLEMENT_FILENAME_PREFIX;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COSTS_ORDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_ABSOLUTE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocument;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getObjectMapperInstance;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.getCollectionMembersOrEmptyList;

@RunWith(MockitoJUnitRunner.class)
public class CourtOrderDocumentsUpdateServiceImplTest {

    @Mock
    private DocumentGenerationWorkflow documentGenerationWorkflow;

    private CourtOrderDocumentsUpdateService classUnderTest;

    private CaseDetails incomingCaseDetails;

    @Before
    public void setUp() {
        classUnderTest = new CourtOrderDocumentsUpdateServiceImpl(getObjectMapperInstance(), documentGenerationWorkflow);

        Map<String, Object> incomingCaseData = Map.of(D8DOCUMENTS_GENERATED, List.of(
            createCollectionMemberDocument("oldUrl", DOCUMENT_TYPE_COE, CERTIFICATE_OF_ENTITLEMENT_FILENAME_PREFIX),
            createCollectionMemberDocument("oldUrl", COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER_DOCUMENT_TYPE),
            createCollectionMemberDocument("oldUrl", DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI_FILENAME),
            createCollectionMemberDocument("oldUrl", DECREE_ABSOLUTE_DOCUMENT_TYPE, DECREE_ABSOLUTE_FILENAME)
        ));
        incomingCaseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(incomingCaseData).build();
    }

    @Test
    public void shouldUpdateExistingCourtOrderDocuments() throws WorkflowException, CaseOrchestrationServiceException {
        mockWorkflowToAddOrReplaceDocument(DOCUMENT_TYPE_COE, COE, CERTIFICATE_OF_ENTITLEMENT_FILENAME_PREFIX);
        mockWorkflowToAddOrReplaceDocument(COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER, COSTS_ORDER_DOCUMENT_TYPE);
        mockWorkflowToAddOrReplaceDocument(DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI, DECREE_NISI_FILENAME);
        mockWorkflowToAddOrReplaceDocument(DECREE_ABSOLUTE_DOCUMENT_TYPE, DECREE_ABSOLUTE, DECREE_ABSOLUTE_FILENAME);

        Map<String, Object> returnedPayload = classUnderTest.updateExistingCourtOrderDocuments(AUTH_TOKEN, incomingCaseDetails);

        List<CollectionMember<Document>> generatedDocuments =
            getCollectionMembersOrEmptyList(getObjectMapperInstance(), returnedPayload, D8DOCUMENTS_GENERATED);
        assertThat(generatedDocuments.size(), is(4));
        assertDocumentIsNew(generatedDocuments.get(0), DOCUMENT_TYPE_COE);
        assertDocumentIsNew(generatedDocuments.get(1), COSTS_ORDER_DOCUMENT_TYPE);
        assertDocumentIsNew(generatedDocuments.get(2), DECREE_NISI_DOCUMENT_TYPE);
        assertDocumentIsNew(generatedDocuments.get(3), DECREE_ABSOLUTE_DOCUMENT_TYPE);

        verify(documentGenerationWorkflow)
            .run(incomingCaseDetails, AUTH_TOKEN, DOCUMENT_TYPE_COE, COE, CERTIFICATE_OF_ENTITLEMENT_FILENAME_PREFIX);
        verify(documentGenerationWorkflow)
            .run(incomingCaseDetails, AUTH_TOKEN, COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER, COSTS_ORDER_DOCUMENT_TYPE);
        verify(documentGenerationWorkflow)
            .run(incomingCaseDetails, AUTH_TOKEN, DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI, DECREE_NISI_FILENAME);
        verify(documentGenerationWorkflow)
            .run(incomingCaseDetails, AUTH_TOKEN, DECREE_ABSOLUTE_DOCUMENT_TYPE, DECREE_ABSOLUTE, DECREE_ABSOLUTE_FILENAME);
    }

    @Test
    public void shouldNotUpdateCourtOrderDocumentsIfTheyDoNotAlreadyExist() throws CaseOrchestrationServiceException {
        Map<String, Object> returnedPayload = classUnderTest.updateExistingCourtOrderDocuments(AUTH_TOKEN, TEST_INCOMING_CASE_DETAILS);

        assertThat(returnedPayload, equalTo(TEST_INCOMING_CASE_DETAILS.getCaseData()));

        verifyNoInteractions(documentGenerationWorkflow);
    }

    @Test
    public void shouldThrowCaseOrchestrationExceptionIfCertificateOfEntitlementGenerationFails() throws WorkflowException {
        when(documentGenerationWorkflow.run(incomingCaseDetails, AUTH_TOKEN, DOCUMENT_TYPE_COE, COE, CERTIFICATE_OF_ENTITLEMENT_FILENAME_PREFIX))
            .thenThrow(WorkflowException.class);

        CaseOrchestrationServiceException serviceException = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> classUnderTest.updateExistingCourtOrderDocuments(AUTH_TOKEN, incomingCaseDetails)
        );

        assertThat(serviceException.getCaseId().orElseThrow(), is(TEST_CASE_ID));
        assertThat(serviceException.getCause(), isA(WorkflowException.class));
    }

    @Test
    public void shouldThrowCaseOrchestrationExceptionIfCostsOrderGenerationFails() throws WorkflowException {
        when(documentGenerationWorkflow.run(incomingCaseDetails, AUTH_TOKEN, COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER, COSTS_ORDER_DOCUMENT_TYPE))
            .thenThrow(WorkflowException.class);

        CaseOrchestrationServiceException serviceException = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> classUnderTest.updateExistingCourtOrderDocuments(AUTH_TOKEN, incomingCaseDetails)
        );

        assertThat(serviceException.getCaseId().orElseThrow(), is(TEST_CASE_ID));
        assertThat(serviceException.getCause(), isA(WorkflowException.class));
    }

    @Test
    public void shouldThrowCaseOrchestrationExceptionIfDecreeNisiGenerationFails() throws WorkflowException {
        when(documentGenerationWorkflow.run(incomingCaseDetails, AUTH_TOKEN, DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI, DECREE_NISI_FILENAME))
            .thenThrow(WorkflowException.class);

        CaseOrchestrationServiceException serviceException = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> classUnderTest.updateExistingCourtOrderDocuments(AUTH_TOKEN, incomingCaseDetails)
        );

        assertThat(serviceException.getCaseId().orElseThrow(), is(TEST_CASE_ID));
        assertThat(serviceException.getCause(), isA(WorkflowException.class));
    }

    @Test
    public void shouldThrowCaseOrchestrationExceptionIfDecreeAbsoluteGenerationFails() throws WorkflowException {
        when(documentGenerationWorkflow.run(
            incomingCaseDetails, AUTH_TOKEN, DECREE_ABSOLUTE_DOCUMENT_TYPE, DECREE_ABSOLUTE, DECREE_ABSOLUTE_FILENAME
        )).thenThrow(WorkflowException.class);

        CaseOrchestrationServiceException serviceException = assertThrows(
            CaseOrchestrationServiceException.class,
            () -> classUnderTest.updateExistingCourtOrderDocuments(AUTH_TOKEN, incomingCaseDetails)
        );

        assertThat(serviceException.getCaseId().orElseThrow(), is(TEST_CASE_ID));
        assertThat(serviceException.getCause(), isA(WorkflowException.class));
    }

    private void mockWorkflowToAddOrReplaceDocument(String expectedCcdDocumentType,
                                                    DocumentType expectedDocumentType,
                                                    String expectedFilename) throws WorkflowException {
        when(documentGenerationWorkflow.run(any(), eq(AUTH_TOKEN), eq(expectedCcdDocumentType), eq(expectedDocumentType), eq(expectedFilename)))
            .thenAnswer(invocation -> {
                CaseDetails caseDetails = invocation.getArgument(0, CaseDetails.class);
                Map<String, Object> caseData = new HashMap<>(caseDetails.getCaseData());

                List<CollectionMember<Document>> generatedDocuments =
                    getCollectionMembersOrEmptyList(getObjectMapperInstance(), caseData, D8DOCUMENTS_GENERATED);
                generatedDocuments = generatedDocuments.stream()
                    .filter(not(documentCollectionMember -> expectedCcdDocumentType.equals(documentCollectionMember.getValue().getDocumentType())))
                    .collect(Collectors.toList());
                generatedDocuments.add(createCollectionMemberDocument("newUrl", expectedCcdDocumentType, expectedFilename));

                caseData.put(D8DOCUMENTS_GENERATED, generatedDocuments);

                return caseData;
            });
    }

    private void assertDocumentIsNew(CollectionMember<Document> document, String expectedCcdDocumentType) {
        assertThat(document.getValue().getDocumentType(), is(expectedCcdDocumentType));
        assertThat(document.getValue().getDocumentLink().getDocumentUrl(), is("newUrl"));
    }

}