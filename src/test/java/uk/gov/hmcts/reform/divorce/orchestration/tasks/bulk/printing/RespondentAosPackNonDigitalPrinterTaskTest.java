package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

@RunWith(MockitoJUnitRunner.class)
public class RespondentAosPackNonDigitalPrinterTaskTest {

    private static final String RESPONDENT_LETTER_TYPE = "respondent-aos-pack-non-digital";
    private static final List<String> DOCUMENT_TYPES_TO_PRINT = asList(DOCUMENT_TYPE_PETITION);

    @Mock
    private BulkPrinterTask bulkPrinterTask;

    @InjectMocks
    private RespondentAosPackNonDigitalPrinterTask classUnderTest;

    @Test
    public void shouldCallBulkPrinterWithSpecificParameters() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject("testKey", "testValue");
        final Map<String, Object> payload = emptyMap();

        final Map<String, Object> result = classUnderTest.execute(context, payload);

        assertThat(result, is(payload));
        verify(bulkPrinterTask).printSpecifiedDocument(context, payload, RESPONDENT_LETTER_TYPE, DOCUMENT_TYPES_TO_PRINT);
    }
}
