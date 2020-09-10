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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.class)
public class GeneralOrderServiceImplTest {

    @Mock
    private GenerateGeneralOrderWorkflow generateGeneralOrderWorkflow;

    @Mock
    private GenerateGeneralOrderDraftWorkflow generateGeneralOrderDraftWorkflow;

    @InjectMocks
    private GeneralOrderServiceImpl generalOrdersService;

    private final CaseDetails caseDetails = CaseDetails.builder().caseId(TEST_CASE_ID).caseData(EMPTY_MAP).build();

    @Test
    public void generateGeneralOrderShouldCallWorkflow() throws GeneralOrderServiceException, WorkflowException {
        CaseDetails response = generalOrdersService.generateGeneralOrder(caseDetails, AUTH_TOKEN);

        verify(generateGeneralOrderWorkflow).run(caseDetails, AUTH_TOKEN);
        assertNotNull(response.getCaseData());
    }

    @Test
    public void generateGeneralOrderShouldThrowExceptionWithCaseId() throws WorkflowException {
        when(generateGeneralOrderWorkflow.run(caseDetails, AUTH_TOKEN)).thenThrow(WorkflowException.class);

        try {
            generalOrdersService.generateGeneralOrder(caseDetails, AUTH_TOKEN);
            fail();
        } catch (GeneralOrderServiceException exception) {
            assertThat(exception.getCaseId().isPresent(), is(true));
            assertThat(exception.getCaseId().get(), is(caseDetails.getCaseId()));
        }
    }

    @Test
    public void generateGeneralOrderDraft() throws GeneralOrderServiceException, WorkflowException {
        when(generateGeneralOrderDraftWorkflow.run(caseDetails, AUTH_TOKEN)).thenReturn(caseDetails.getCaseData());

        CaseDetails response = generalOrdersService.generateGeneralOrderDraft(caseDetails, AUTH_TOKEN);

        assertNotNull(response.getCaseData());
    }

    @Test
    public void generateGeneralOrderDraftShouldThrowExceptionWithCaseId() throws WorkflowException {
        when(generateGeneralOrderDraftWorkflow.run(caseDetails, AUTH_TOKEN)).thenThrow(WorkflowException.class);

        try {
            generalOrdersService.generateGeneralOrderDraft(caseDetails, AUTH_TOKEN);
            fail();
        } catch (GeneralOrderServiceException exception) {
            assertThat(exception.getCaseId().isPresent(), is(true));
            assertThat(exception.getCaseId().get(), is(caseDetails.getCaseId()));
        }
    }
}
