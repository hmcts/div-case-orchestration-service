package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CreateAmendPetitionDraftForRefusal;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateCaseInCCD;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.class)
public class AmendPetitionForRefusalWorkflowTest {

    @Mock
    private CreateAmendPetitionDraftForRefusal amendPetitionDraftForRefusal;

    @Mock
    private UpdateCaseInCCD updateCaseInCCD;

    @InjectMocks
    private AmendPetitionForRefusalWorkflow classUnderTest;

    @Test
    public void whenAmendPetitionForRefusal_thenProcessAsExpected() throws WorkflowException {
        final Map<String, Object> caseData = new HashMap<>();

        when(amendPetitionDraftForRefusal.execute(any(), anyMap())).thenReturn(caseData);
        when(updateCaseInCCD.execute(any(), eq(caseData))).thenReturn(caseData);

        classUnderTest.run(TEST_CASE_ID, AUTH_TOKEN);

        verify(amendPetitionDraftForRefusal).execute(any(), eq(Collections.emptyMap()));
        verify(updateCaseInCCD).execute(any(), eq(caseData));
    }
}