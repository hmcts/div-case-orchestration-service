package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ISSUE_DATE;

@RunWith(MockitoJUnitRunner.class)
public class SetIssueDateTest {

    private static final String EXPECTED_DATE = "2019-05-11";

    @InjectMocks
    private SetIssueDate setIssueDate;

    @Mock
    private CcdUtil ccdUtil;

    @Before
    public void setup() {
        when(ccdUtil.getCurrentDateCcdFormat()).thenReturn(EXPECTED_DATE);
    }

    @Test
    public void testGenerateIssueDateSetsDateToNow() {
        HashMap<String, Object> payload = new HashMap<>();

        setIssueDate.execute(null, payload);

        assertEquals(EXPECTED_DATE, payload.get(ISSUE_DATE));
    }
}