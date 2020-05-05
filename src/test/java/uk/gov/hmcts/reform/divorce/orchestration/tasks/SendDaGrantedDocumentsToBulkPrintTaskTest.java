package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinter;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.UNFORMATTED_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;

@RunWith(MockitoJUnitRunner.class)
public class SendDaGrantedDocumentsToBulkPrintTaskTest extends TestCase {
    private TaskContext context;
    private Map<String, Object> testData;

    @Mock
    BulkPrinter bulkPrinter;

    @InjectMocks
    SendDaGrantedDocumentsToBulkPrintTask daGrantedDocumentsToBulkPrintTask;

    @Before
    public void setup() {
        testData = populateCaseData();

        context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, UNFORMATTED_CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
        context.setTransientObject(CASE_DETAILS_JSON_KEY, populateCaseDetails());
        context.setTransientObject(DOCUMENT_TYPE, DECREE_ABSOLUTE_LETTER_DOCUMENT_TYPE);
        context.setTransientObject(DOCUMENT_TEMPLATE_ID, DECREE_ABSOLUTE_LETTER_TEMPLATE_ID);
        context.setTransientObject(DOCUMENT_FILENAME, DECREE_ABSOLUTE_LETTER_FILENAME);
    }

    @Test
    public void testDaGrantedReturnsCorrectResult() {
        when(bulkPrinter.printSpecifiedDocument(any(TaskContext.class), anyMap(), anyString(), anyList())).thenReturn(testData);

        Map<String, Object> result = daGrantedDocumentsToBulkPrintTask.execute(context, testData);

        assertEquals(result, testData);
    }

    private CaseDetails populateCaseDetails() {
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .caseData(testData)
            .build();
        return caseDetails;
    }

    private Map<String, Object> populateCaseData() {
        final Map<String, Object> expectedResponse = new HashMap<>(
            ImmutableMap.of(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE)
        );
        return expectedResponse;
    }
}