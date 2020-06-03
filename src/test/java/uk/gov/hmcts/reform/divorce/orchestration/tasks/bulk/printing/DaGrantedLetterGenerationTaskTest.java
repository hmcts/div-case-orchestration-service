package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.BasicCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.DocumentContentFetcherService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor;

import java.time.LocalDate;
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
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DaGrantedLetterGenerationTask.FileMetadata.TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.PrepareDataForDocumentGenerationTaskTest.document;

@RunWith(MockitoJUnitRunner.class)
public class DaGrantedLetterGenerationTaskTest {

    private static final String PETITIONERS_FIRST_NAME = "Anna";
    private static final String PETITIONERS_LAST_NAME = "Nowak";

    private static final String CASE_ID = "It's mandatory field in context";
    private static final String LETTER_DATE = LocalDate.now().toString();

    private static final CtscContactDetails CTSC_CONTACT = CtscContactDetails.builder().build();
    private static final GeneratedDocumentInfo DOCUMENT = GeneratedDocumentInfo
        .builder()
        .documentType(DaGrantedLetterGenerationTask.FileMetadata.DOCUMENT_TYPE)
        .fileName(DaGrantedLetterGenerationTask.FileMetadata.FILE_NAME)
        .build();

    @Mock
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Mock
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    @Mock
    private DocumentContentFetcherService documentContentFetcherService;

    @InjectMocks
    private DaGrantedLetterGenerationTask daGrantedLetterGenerationTask;

    @Before
    public void setup() {
        GeneratedDocumentInfo createdDoc = document();
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
        when(pdfDocumentGenerationService.generatePdf(any(DocmosisTemplateVars.class), eq(TEMPLATE_ID), eq(AUTH_TOKEN)))
            .thenReturn(createdDoc);
        when(documentContentFetcherService.fetchPrintContent(createdDoc)).thenReturn(DOCUMENT);
    }

    @Test
    public void executeShouldPopulateFieldInContext() throws TaskException {
        TaskContext context = prepareTaskContext();

        daGrantedLetterGenerationTask.execute(context, buildCaseData());

        Map<String, GeneratedDocumentInfo> documents = PrepareDataForDocumentGenerationTask.getDocumentsToBulkPrint(context);

        assertThat(documents.size(), is(1));
        assertThat(documents.get(DOCUMENT.getDocumentType()), is(DOCUMENT));
        verify(ctscContactDetailsDataProviderService, times(1)).getCtscContactDetails();
        verifyPdfDocumentGenerationCallIsCorrect();
    }

    private void verifyPdfDocumentGenerationCallIsCorrect() {
        final ArgumentCaptor<BasicCoverLetter> daGrantedLetterArgumentCaptor = ArgumentCaptor.forClass(BasicCoverLetter.class);
        verify(pdfDocumentGenerationService, times(1))
            .generatePdf(daGrantedLetterArgumentCaptor.capture(), eq(TEMPLATE_ID), eq(AUTH_TOKEN));
        verify(documentContentFetcherService, times(1)).fetchPrintContent(eq(DOCUMENT));

        final BasicCoverLetter daGrantedLetter = daGrantedLetterArgumentCaptor.getValue();
        assertThat(daGrantedLetter.getPetitionerFullName(), is("Anna Nowak"));
        assertThat(daGrantedLetter.getRespondentFullName(), is("John Smith"));
        assertThat(daGrantedLetter.getCaseReference(), is(CASE_ID));
        assertThat(daGrantedLetter.getLetterDate(), is(LETTER_DATE));
        assertThat(daGrantedLetter.getCtscContactDetails(), is(CTSC_CONTACT));
    }

    public static TaskContext prepareTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        return context;
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithRespondentAsAddressee();
        caseData.put(DaGrantedLetterDataExtractor.CaseDataKeys.DA_GRANTED_DATE, LETTER_DATE);

        caseData.put(PETITIONER_FIRST_NAME, PETITIONERS_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, PETITIONERS_LAST_NAME);

        return caseData;
    }
}
