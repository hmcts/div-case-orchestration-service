package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.exception.GeneralOrderServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.ServiceJourneyServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorder.GenerateGeneralOrderDraftWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.generalorder.GenerateGeneralOrderWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.MakeServiceDecisionDateWorkflow;

import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class GeneralOrdersServiceImplTest {

    @Mock
    private GenerateGeneralOrderWorkflow generateGeneralOrderWorkflow;

    @Mock
    private GenerateGeneralOrderDraftWorkflow generateGeneralOrderDraftWorkflow;

    @InjectMocks
    private GeneralOrdersServiceImpl generalOrdersService;

    private final CaseDetails caseDetails = CaseDetails.builder().caseData(EMPTY_MAP).build();

    @Test
    public void generateGeneralOrderShouldCallWorkflow() throws GeneralOrderServiceException, WorkflowException {
        CcdCallbackResponse response = generalOrdersService.generateGeneralOrder(caseDetails, AUTH_TOKEN);

        verify(generateGeneralOrderWorkflow).run(caseDetails, AUTH_TOKEN);
        assertNotNull(response.getData());
    }

    @Test(expected = GeneralOrderServiceException.class)
    public void generateGeneralOrderShouldThrowException() throws GeneralOrderServiceException, WorkflowException {
        when(generateGeneralOrderWorkflow.run(caseDetails, AUTH_TOKEN)).thenThrow(WorkflowException.class);

        generalOrdersService.generateGeneralOrder(caseDetails, AUTH_TOKEN);
    }

    @Test
    public void generateGeneralOrderDraft() throws GeneralOrderServiceException, WorkflowException  {
        when(generateGeneralOrderDraftWorkflow.run(caseDetails, AUTH_TOKEN)).thenReturn(caseDetails.getCaseData());

        CcdCallbackResponse response = generalOrdersService.generateGeneralOrderDraft(caseDetails, AUTH_TOKEN);

        assertNotNull(response.getData());
    }

    @Test(expected = GeneralOrderServiceException.class)
    public void generateGeneralOrderDraftShouldThrowException() throws GeneralOrderServiceException, WorkflowException {
        when(generateGeneralOrderDraftWorkflow.run(caseDetails, AUTH_TOKEN)).thenThrow(WorkflowException.class);

        generalOrdersService.generateGeneralOrderDraft(caseDetails, AUTH_TOKEN);
    }
}
