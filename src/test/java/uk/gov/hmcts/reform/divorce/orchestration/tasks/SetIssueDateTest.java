package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;

import java.time.LocalDate;
import java.util.HashMap;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ISSUE_DATE;

public class SetIssueDateTest {

    private final SetIssueDate setIssueDate = new SetIssueDate();

    @Test
    public void testGenerateIssueDateSetsDateToNow() {
        HashMap<String, Object> payload = new HashMap<>();

        setIssueDate.execute(null, payload);

        String expectedDate = LocalDate.now().format(ofPattern(CCD_DATE_FORMAT));
        assertEquals(expectedDate, payload.get(ISSUE_DATE));
    }
}