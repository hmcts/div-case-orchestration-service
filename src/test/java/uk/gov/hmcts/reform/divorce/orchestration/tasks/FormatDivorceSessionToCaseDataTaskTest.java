package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.service.DataMapTransformer;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_INCOMING_PAYLOAD;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PAYLOAD_TO_RETURN;

@RunWith(MockitoJUnitRunner.class)
public class FormatDivorceSessionToCaseDataTaskTest {

    @Mock
    private DataMapTransformer dataMapTransformer;

    @InjectMocks
    private FormatDivorceSessionToCaseDataTask formatDivorceSessionToCaseDataTask;

    @Test
    public void shouldCallCaseFormatterClientTransformToCCDFormat() {
        when(dataMapTransformer.transformDivorceCaseDataToCourtCaseData(any())).thenReturn(TEST_PAYLOAD_TO_RETURN);

        Map<String, Object> returnedCoreCaseData = formatDivorceSessionToCaseDataTask.execute(null, TEST_INCOMING_PAYLOAD);

        assertThat(returnedCoreCaseData, is(TEST_PAYLOAD_TO_RETURN));
        verify(dataMapTransformer).transformDivorceCaseDataToCourtCaseData(TEST_INCOMING_PAYLOAD);
    }

}