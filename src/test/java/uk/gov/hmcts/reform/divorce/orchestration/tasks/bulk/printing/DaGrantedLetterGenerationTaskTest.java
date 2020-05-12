package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractorTest.buildCaseDataWithAddressee;

@RunWith(MockitoJUnitRunner.class)
public class DaGrantedLetterGenerationTaskTest {

    @Mock
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Mock
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    @InjectMocks
    private DaGrantedLetterGenerationTask daGrantedLetterGenerationTask;

    @Before
    public void setup() {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CtscContactDetails.builder().build());
        when(pdfDocumentGenerationService.generatePdf(
            any(DocmosisTemplateVars.class), eq(DaGrantedLetterGenerationTask.FileMetadata.TEMPLATE_ID), eq(AUTH_TOKEN))
        ).thenReturn(GeneratedDocumentInfo.builder().build());
    }

    @Test
    public void executeShouldPopulateFieldInContext() throws TaskException {
        TaskContext context = prepareTaskContext();

        daGrantedLetterGenerationTask.execute(context, buildCaseData());

        List<GeneratedDocumentInfo> documents = context
            .getTransientObject(PrepareDataForDocumentGenerationTask.ContextKeys.GENERATED_DOCUMENTS);

        assertThat(documents.size(), is(1));
        verify(ctscContactDetailsDataProviderService, times(1)).getCtscContactDetails();
        verify(pdfDocumentGenerationService, times(1)).generatePdf(any(), any(), any());
    }

    public static TaskContext prepareTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, "It's mandatory field in context");
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        return context;
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = buildCaseDataWithAddressee();
        caseData.put(DaGrantedLetterDataExtractor.CaseDataKeys.DA_GRANTED_DATE, "201-01-10");

        return caseData;
    }
}
