package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseLink;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMENDED_CASE_ID_CCD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;

@RunWith(MockitoJUnitRunner.class)
public class SetAmendedCaseIdTaskTest {

    @InjectMocks
    private SetAmendedCaseIdTask setAmendedCaseIdTask;

    @Test
    public void executeShouldSetAmendedCaseId() {
        final Map<String, Object> oldCaseData = new HashMap<>();
        final Map<String, Object> newCaseData = new HashMap<>(ImmutableMap.of(ID, TEST_CASE_ID));
        final Map<String, Object> expectedCaseData = new HashMap<>(ImmutableMap.of(
            AMENDED_CASE_ID_CCD_KEY, new CaseLink(TEST_CASE_ID)
        ));

        final TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CCD_CASE_DATA, oldCaseData);

        setAmendedCaseIdTask.execute(context, newCaseData);

        assertEquals(expectedCaseData, context.getTransientObject(CCD_CASE_DATA));

    }
}
