package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.HashMap;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class SetIssueDateTest {

    private SetIssueDate setIssueDate = new SetIssueDate();

    @Test
    public void testGenerateIssueDateSetsDateToNow() {
        HashMap<String, Object> payload = new HashMap<>();

        setIssueDate.execute(null, payload);

        String expectedDate = LocalDate.now().format(ofPattern("yyyy-MM-dd"));
        assertEquals(expectedDate, payload.get("IssueDate"));
    }
}