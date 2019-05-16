package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PopulateMiniPetitionUrl;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SolicitorDnReviewPetitionWorkflowTest {

    @Mock
    PopulateMiniPetitionUrl populateMiniPetitionUrl;

    @InjectMocks
    SolicitorDnReviewPetitionWorkflow solicitorDnReviewPetitionWorkflow;

    private TaskContext taskContext;

    @Before
    public void setup() {
        taskContext = new DefaultTaskContext();
    }

    @Test
    public void runShouldExecuteTasksAndReturnPayload() throws Exception {

        Map<String, Object> caseData = Collections.emptyMap();

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(caseData)
                .build();

        when(populateMiniPetitionUrl.execute(taskContext, caseData)).thenReturn(caseData);

        assertThat(solicitorDnReviewPetitionWorkflow.run(caseDetails), is(caseData));

        verify(populateMiniPetitionUrl).execute(taskContext, caseData);
    }
}