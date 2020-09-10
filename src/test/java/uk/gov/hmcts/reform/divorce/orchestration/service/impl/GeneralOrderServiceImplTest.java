package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.exception.GeneralOrderServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorder.GenerateGeneralOrderDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorder.GenerateGeneralOrderWorkflow;

import static java.util.Collections.EMPTY_MAP;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class GeneralOrderServiceImplTest {

    @Mock
    private GenerateGeneralOrderWorkflow generateGeneralOrderWorkflow;

    @Mock
    private GenerateGeneralOrderDraftWorkflow generateGeneralOrderDraftWorkflow;

    @InjectMocks
    private GeneralOrderServiceImpl generalOrdersService;

    private final CaseDetails caseDetails = CaseDetails.builder().caseData(EMPTY_MAP).build();

    @Test
    public void generateGeneralOrderShouldCallWorkflow() throws GeneralOrderServiceException, WorkflowException {
        CaseDetails response = generalOrdersService.generateGeneralOrder(caseDetails, AUTH_TOKEN);

        verify(generateGeneralOrderWorkflow).run(caseDetails, AUTH_TOKEN);
        assertNotNull(response.getCaseData());
    }

    @Test(expected = GeneralOrderServiceException.class)
    public void generateGeneralOrderShouldThrowException() throws GeneralOrderServiceException, WorkflowException {
        when(generateGeneralOrderWorkflow.run(caseDetails, AUTH_TOKEN)).thenThrow(WorkflowException.class);

        generalOrdersService.generateGeneralOrder(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void generateGeneralOrderDraft() throws GeneralOrderServiceException, WorkflowException {
        when(generateGeneralOrderDraftWorkflow.run(caseDetails, AUTH_TOKEN)).thenReturn(caseDetails.getCaseData());

        CaseDetails response = generalOrdersService.generateGeneralOrderDraft(caseDetails, AUTH_TOKEN);

        assertNotNull(response.getCaseData());
    }

    @Test(expected = GeneralOrderServiceException.class)
    public void generateGeneralOrderDraftShouldThrowException() throws GeneralOrderServiceException, WorkflowException {
        when(generateGeneralOrderDraftWorkflow.run(caseDetails, AUTH_TOKEN)).thenThrow(WorkflowException.class);

        generalOrdersService.generateGeneralOrderDraft(caseDetails, AUTH_TOKEN);
    }
}
