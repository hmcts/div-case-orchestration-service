package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INCOMING_PAYLOAD;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PAYLOAD_TO_RETURN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataToDivorceFormatterTaskTest {

    @Mock
    private CaseFormatterClient caseFormatterClient;

    @InjectMocks
    private CaseDataToDivorceFormatterTask classUnderTest;

    @Test
    public void whenFormatData_thenReturnExpectedData() {
        final DefaultTaskContext context = new DefaultTaskContext();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        final Map<String, Object> expectedResults = TEST_PAYLOAD_TO_RETURN;
        when(caseFormatterClient.transformToDivorceFormat(AUTH_TOKEN, TEST_INCOMING_PAYLOAD)).thenReturn(expectedResults);

        Map<String, Object> returnedCaseData = classUnderTest.execute(context, TEST_INCOMING_PAYLOAD);

        assertThat(returnedCaseData, is(expectedResults));
        verify(caseFormatterClient).transformToDivorceFormat(AUTH_TOKEN, TEST_INCOMING_PAYLOAD);
    }
}
