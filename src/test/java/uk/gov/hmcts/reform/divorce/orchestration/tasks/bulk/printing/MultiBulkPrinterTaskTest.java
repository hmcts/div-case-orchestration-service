package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.BulkPrintConfig;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.MultiBulkPrinterTask.ContextFields.MULTI_BULK_PRINT_CONFIGS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

@RunWith(MockitoJUnitRunner.class)
public class MultiBulkPrinterTaskTest {

    private static final List<String> bulkLetterTypes = asList("letter", "letter2", "letter3");
    private static final List<List<String>> documents = asList(
        asList("a"),
        asList("b", "d"),
        asList("x", "t")
    );

    @Mock
    private BulkPrinterTask bulkPrinterTask;

    @InjectMocks
    private MultiBulkPrinterTask multiBulkPrinterTask;

    @Test
    public void executeShouldNotCallBulkPrintingWhenNoMultiBulkPrintingConfig() throws TaskException {

        multiBulkPrinterTask.execute(contextWithToken(), new HashMap<>());

        verify(bulkPrinterTask, never()).printSpecifiedDocument(any(), any(), any(), any());
    }

    @Test
    public void executeShouldCallBulkPrintingOnceWhenNoMultiBulkPrintingConfigHasOneElement() throws TaskException {
        TaskContext taskContext = contextWithToken();
        taskContext.setTransientObject(
            MULTI_BULK_PRINT_CONFIGS,
            asList(new BulkPrintConfig(bulkLetterTypes.get(0), documents.get(0)))
        );
        Map<String, Object> caseData = new HashMap<>();

        multiBulkPrinterTask.execute(taskContext, caseData);

        verify(bulkPrinterTask, times(1))
            .printSpecifiedDocument(
                eq(taskContext),
                eq(caseData),
                eq(bulkLetterTypes.get(0)),
                eq(documents.get(0))
            );
    }

    @Test
    public void executeShouldCallBulkPrintingManyTimes() throws TaskException {
        TaskContext taskContext = contextWithToken();
        taskContext.setTransientObject(
            MULTI_BULK_PRINT_CONFIGS,
            asList(
                new BulkPrintConfig(bulkLetterTypes.get(0), documents.get(0)),
                new BulkPrintConfig(bulkLetterTypes.get(1), documents.get(1)),
                new BulkPrintConfig(bulkLetterTypes.get(2), documents.get(2))
            )
        );
        Map<String, Object> caseData = new HashMap<>();

        multiBulkPrinterTask.execute(taskContext, caseData);

        for (int i = 0; i < bulkLetterTypes.size(); i++) {
            verify(bulkPrinterTask, times(1))
                .printSpecifiedDocument(
                    eq(taskContext),
                    eq(caseData),
                    eq(bulkLetterTypes.get(i)),
                    eq(documents.get(i)));
        }
    }

    @Test
    public void executeShouldNotCallBulkPrintingWhenMisconfigured() throws TaskException {
        TaskContext taskContext = contextWithToken();
        taskContext.setTransientObject(
            MULTI_BULK_PRINT_CONFIGS,
            asList(
                new BulkPrintConfig(bulkLetterTypes.get(0), Collections.emptyList()),
                new BulkPrintConfig(bulkLetterTypes.get(1), Collections.emptyList())
            )
        );

        multiBulkPrinterTask.execute(taskContext, new HashMap<>());

        verify(bulkPrinterTask, never()).printSpecifiedDocument(any(), any(), any(), any());
    }
}
