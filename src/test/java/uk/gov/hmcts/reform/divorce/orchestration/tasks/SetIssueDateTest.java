package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ISSUE_DATE;

public class SetIssueDateTest {

    private final SetIssueDate setIssueDate = new SetIssueDate();

    @Test
    public void testGenerateIssueDateSetsDateToNow() {
        HashMap<String, Object> payload = new HashMap<>();

        setIssueDate.execute(null, payload);

        String expectedDate = CcdUtil.getCurrentDate();
        assertEquals(expectedDate, payload.get(ISSUE_DATE));
    }
}