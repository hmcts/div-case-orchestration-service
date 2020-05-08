package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DaGrantedLetter;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;

import java.util.Map;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractorTest.buildCaseDataWithAddressee;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.PrepareDataForDocumentGenerationTask.ContextKeys.PREPARED_DATA_FOR_DOCUMENT_GENERATION;

@RunWith(MockitoJUnitRunner.class)
public class PrepareDataForDaGrantedLetterGenerationTaskTest {

    @Mock
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @InjectMocks
    private PrepareDataForDaGrantedLetterGenerationTask prepareDataForDaGrantedLetterGenerationTask;

    @Before
    public void setup() {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CtscContactDetails.builder().build());
    }

    @Test
    public void addPreparedDataToContext() throws TaskException {
        TaskContext context = prepareTaskContext();

        prepareDataForDaGrantedLetterGenerationTask.addPreparedDataToContext(context, buildCaseData());

        assertThat(context.getTransientObject(PREPARED_DATA_FOR_DOCUMENT_GENERATION), instanceOf(DaGrantedLetter.class));
        verify(ctscContactDetailsDataProviderService, times(1)).getCtscContactDetails();
    }

    private TaskContext prepareTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject("caseId", "It's mandatory field in context");

        return context;
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = buildCaseDataWithAddressee();
        caseData.put("DecreeAbsoluteGrantedDate", "201-01-10");

        return caseData;
    }
}