package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.LAST_SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.LAST_SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class CopyServiceApplicationDataToRetainTaskTest {

    @InjectMocks
    private CopyServiceApplicationDataToRetainTask copyServiceApplicationDataToRetainTask;

    @Test
    public void shouldExecuteAndCopyDataToRetainToNewFields() {
        Map<String, Object> input = buildCaseData();

        Map<String, Object> output = copyServiceApplicationDataToRetainTask.execute(context(), input);

        assertThat(output.get(LAST_SERVICE_APPLICATION_GRANTED), is(YES_VALUE));
        assertThat(output.get(LAST_SERVICE_APPLICATION_TYPE), is(ApplicationServiceTypes.DEEMED));
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.DEEMED);
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, YES_VALUE);

        return caseData;
    }
}
