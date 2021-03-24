package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.SingleCaseDocumentGenerationWorkflow;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_ENDCLAIM_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_OPTIONS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class DecreeNisiServiceImplTest {

    @Mock
    private SingleCaseDocumentGenerationWorkflow singleCaseDocumentGenerationWorkflow;

    @InjectMocks
    private DecreeNisiServiceImpl classUnderTest;

    @Mock
    private CcdCallbackRequest ccdCallbackRequest;

    private Map<String, Object> requestPayload;

    @Before
    public void setUp() {
        requestPayload = singletonMap("requestPayloadKey", "requestPayloadValue");
        ccdCallbackRequest = buildCcdCallbackRequest(requestPayload);
    }

    @Test
    public void shouldCallTheRightWorkflow_ForManualDnPronouncementDocumentGeneration()
        throws WorkflowException, CaseOrchestrationServiceException {

        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(CASE_ID_JSON_KEY, CaseLink.builder().caseReference(TEST_CASE_ID).build());
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, "Yes");

        when(singleCaseDocumentGenerationWorkflow.run(ccdCallbackRequest.getCaseDetails(), AUTH_TOKEN)).thenReturn(caseData);
        assertThat(classUnderTest.handleManualDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN), is(equalTo(caseData)));
    }

    @Test
    public void shouldGenerateNoDocuments_whenCaseIdIsNull()
        throws WorkflowException, CaseOrchestrationServiceException {

        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, "No");

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classUnderTest.handleManualDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        verify(singleCaseDocumentGenerationWorkflow).run(caseDetails, AUTH_TOKEN);
        verifyNoMoreInteractions(singleCaseDocumentGenerationWorkflow);
    }

    @Test
    public void shouldGenerateOnlyManualDnDocuments_WhenPetitionerCostsClaimIsNo()
        throws WorkflowException, CaseOrchestrationServiceException {

        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(CASE_ID_JSON_KEY, CaseLink.builder().caseReference(TEST_CASE_ID).build());
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, "No");

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classUnderTest.handleManualDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        verify(singleCaseDocumentGenerationWorkflow).run(caseDetails, AUTH_TOKEN);
        verifyNoMoreInteractions(singleCaseDocumentGenerationWorkflow);
    }

    @Test
    public void shouldGenerateOnlyManualDnDocuments_WhenPetitionerCostsClaimIsYesButThenPetitionerEndsClaim()
        throws WorkflowException, CaseOrchestrationServiceException {

        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(CASE_ID_JSON_KEY, CaseLink.builder().caseReference(TEST_CASE_ID).build());
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, "Yes");
        caseData.put(DN_COSTS_OPTIONS_CCD_FIELD, DN_COSTS_ENDCLAIM_VALUE);
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "Yes");

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classUnderTest.handleManualDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        verify(singleCaseDocumentGenerationWorkflow).run(caseDetails, AUTH_TOKEN);
        verifyNoMoreInteractions(singleCaseDocumentGenerationWorkflow);
    }

    @Test
    public void shouldGenerateBothManualDocuments_WhenCostsClaimContinues()
        throws WorkflowException, CaseOrchestrationServiceException {

        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(CASE_ID_JSON_KEY, CaseLink.builder().caseReference(TEST_CASE_ID).build());
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, "Yes");
        caseData.put(DN_COSTS_OPTIONS_CCD_FIELD, "Continue");
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "Yes");

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classUnderTest.handleManualDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        verify(singleCaseDocumentGenerationWorkflow).run(caseDetails, AUTH_TOKEN);
        verifyNoMoreInteractions(singleCaseDocumentGenerationWorkflow);
    }

    @Test
    public void shouldGenerateBothManualDocuments_WhenCostsClaimGrantedIsNo()
        throws WorkflowException, CaseOrchestrationServiceException {

        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(CASE_ID_JSON_KEY, CaseLink.builder().caseReference(TEST_CASE_ID).build());
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE);
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, NO_VALUE);

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classUnderTest.handleManualDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        verify(singleCaseDocumentGenerationWorkflow).run(caseDetails, AUTH_TOKEN);
        verifyNoMoreInteractions(singleCaseDocumentGenerationWorkflow);
    }

    @Test
    public void shouldGenerateBothManDocuments_WhenCostsClaimGrantedIsYes()
        throws WorkflowException, CaseOrchestrationServiceException {

        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(CASE_ID_JSON_KEY, CaseLink.builder().caseReference(TEST_CASE_ID).build());
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE);
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE);

        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        classUnderTest.handleManualDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);

        verify(singleCaseDocumentGenerationWorkflow).run(caseDetails, AUTH_TOKEN);
        verifyNoMoreInteractions(singleCaseDocumentGenerationWorkflow);
    }

    @Test(expected = CaseOrchestrationServiceException.class)
    public void shouldThrowException_ForManualDnPronouncedDocumentsGeneration_WhenWorkflowExceptionIsCaught()
        throws WorkflowException, CaseOrchestrationServiceException {

        Map<String, Object> caseData = new HashMap<String, Object>();
        caseData.put(D_8_CASE_REFERENCE, TEST_CASE_ID);
        caseData.put(DIVORCE_COSTS_CLAIM_CCD_FIELD, YES_VALUE);
        caseData.put(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, YES_VALUE);
        CaseDetails caseDetails = CaseDetails.builder().caseData(caseData).build();
        CcdCallbackRequest ccdCallbackRequest = CcdCallbackRequest.builder().caseDetails(caseDetails).build();

        when(singleCaseDocumentGenerationWorkflow.run(caseDetails, AUTH_TOKEN))
            .thenThrow(new WorkflowException("This operation threw an exception"));

        classUnderTest.handleManualDnPronouncementDocumentGeneration(ccdCallbackRequest, AUTH_TOKEN);
    }

    private CcdCallbackRequest buildCcdCallbackRequest(Map<String, Object> requestPayload) {
        return CcdCallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .caseData(requestPayload)
                    .caseId(TEST_CASE_ID)
                    .state(TEST_STATE)
                    .build())
            .eventId(TEST_EVENT_ID)
            .token(TEST_TOKEN)
            .build();
    }

    @After
    public void tearDown() {
        ccdCallbackRequest = null;
        requestPayload = null;
    }
}
