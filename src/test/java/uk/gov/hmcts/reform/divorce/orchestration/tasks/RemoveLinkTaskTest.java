package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;

public class RemoveLinkTaskTest {

    private RemoveLinkTask classToTest = new RemoveLinkTask();

    @Test
    public void testExecuteTaskBulkListingLinkIsRemoved() throws TaskException {
        Map<String, Object> caseData = ImmutableMap.of("anyKey", "anyData",
            BULK_LISTING_CASE_ID_FIELD,"caseLink");
        Map<String, Object> response = classToTest.execute(null, caseData);

        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("anyKey", "anyData");
        assertThat(response, is(expectedMap));
    }
}