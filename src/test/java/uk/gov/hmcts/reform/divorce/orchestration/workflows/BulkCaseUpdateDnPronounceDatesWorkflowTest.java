package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetDnPronouncementDetailsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateDivorceCasePronouncementDateWithinBulk;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_DETAILS_CONTEXT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class BulkCaseUpdateDnPronounceDatesWorkflowTest {

    @Spy
    ObjectMapper objectMapper;

    @Mock
    SetDnPronouncementDetailsTask setDnPronouncementDetailsTask;

    @Mock
    UpdateDivorceCasePronouncementDateWithinBulk updateDivorceCasePronouncementDateWithinBulk;

    @InjectMocks
    BulkCaseUpdateDnPronounceDatesWorkflow bulkCaseUpdateDnPronounceDatesWorkflow;

    private TaskContext context;
    private Map<String, Object> testData;
    private CaseDetails caseDetails;

    @Before
    public void setup() {
        testData = Collections.emptyMap();
        caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .caseData(testData).build();

        Map<String, Object> expectedBulkCaseDetails = ImmutableMap.of(
            CCD_CASE_DATA_FIELD, testData,
            ID, TEST_CASE_ID,
            STATE_CCD_FIELD, TEST_STATE
        );

        context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(BULK_CASE_DETAILS_CONTEXT_KEY, expectedBulkCaseDetails);
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {
        when(setDnPronouncementDetailsTask.execute(context, testData)).thenReturn(testData);
        when(updateDivorceCasePronouncementDateWithinBulk.execute(context, testData)).thenReturn(testData);

        assertEquals(testData, bulkCaseUpdateDnPronounceDatesWorkflow.run(caseDetails, AUTH_TOKEN));

        verify(setDnPronouncementDetailsTask).execute(context, testData);
        verify(updateDivorceCasePronouncementDateWithinBulk).execute(context, testData);
    }
}
