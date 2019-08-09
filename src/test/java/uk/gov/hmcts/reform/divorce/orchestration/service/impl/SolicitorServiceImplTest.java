package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.IssuePersonalServicePackWorkflow;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorServiceImplTest {

    @Mock
    IssuePersonalServicePackWorkflow issuePersonalServicePack;

    @InjectMocks
    SolicitorServiceImpl solicitorService;

    @Test
    public void testIssuePersonalServicePack() throws WorkflowException {
        CcdCallbackRequest request = CcdCallbackRequest.builder()
                .caseDetails(CaseDetails.builder().build())
                .build();

        solicitorService.issuePersonalServicePack(request, TEST_TOKEN);

        verify(issuePersonalServicePack).run(request, TEST_TOKEN);
    }
}
