package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoRespondentCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.DocumentContentFetcherService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CostOrderCoRespondentLetterDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;

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
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CORESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CORESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.PrepareDataForDocumentGenerationTaskTest.document;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentLetterGenerationTask.FileMetadata.TEMPLATE_ID;

public class CostOrderCoRespondentLetterGenerationTaskTest {

    private static final String CO_RESPONDENT_FIRST_NAME = "Anna";
    private static final String CO_RESPONDENT_LAST_NAME = "Nowak";

    private static final String CASE_ID = "It's mandatory field in context";
    private static final String LETTER_DATE = LocalDate.now().toString();

    private static final CtscContactDetails CTSC_CONTACT = CtscContactDetails.builder().build();
    private static final GeneratedDocumentInfo DOCUMENT = GeneratedDocumentInfo
        .builder()
        .documentType(CostOrderCoRespondentLetterGenerationTask.FileMetadata.DOCUMENT_TYPE)
        .fileName(CostOrderCoRespondentLetterGenerationTask.FileMetadata.FILE_NAME)
        .build();

    @Mock
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Mock
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    @Mock
    private DocumentContentFetcherService documentContentFetcherService;

    @InjectMocks
    private CostOrderCoRespondentLetterGenerationTask costOrderCoRespondentLetterGenerationTask;

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

        costOrderCoRespondentLetterGenerationTask.execute(context, buildCaseData());

        Map<String, GeneratedDocumentInfo> documents = PrepareDataForDocumentGenerationTask.getDocumentsToBulkPrint(context);

        assertThat(documents.size(), is(1));
        assertThat(documents.get(DOCUMENT.getDocumentType()), is(DOCUMENT));
        verify(ctscContactDetailsDataProviderService, times(1)).getCtscContactDetails();
        verifyPdfDocumentGenerationCallIsCorrect();
    }

    private void verifyPdfDocumentGenerationCallIsCorrect() {
        final ArgumentCaptor<CoRespondentCoverLetter> CoRespondentCoverLetterArgumentCaptor = ArgumentCaptor.forClass(CoRespondentCoverLetter.class);
        verify(pdfDocumentGenerationService, times(1))
            .generatePdf(CoRespondentCoverLetterArgumentCaptor.capture(), eq(TEMPLATE_ID), eq(AUTH_TOKEN));
        verify(documentContentFetcherService, times(1)).fetchPrintContent(eq(DOCUMENT));

        final CoRespondentCoverLetter CoRespondentCoverLetter = CoRespondentCoverLetterArgumentCaptor.getValue();
        assertThat(CoRespondentCoverLetter.getPetitionerFullName(), is("Anna Nowak"));
        assertThat(CoRespondentCoverLetter.getRespondentFullName(), is("John Smith"));
        assertThat(CoRespondentCoverLetter.getCaseReference(), is(CASE_ID));
        assertThat(CoRespondentCoverLetter.getLetterDate(), is(LETTER_DATE));
        assertThat(CoRespondentCoverLetter.getCtscContactDetails(), is(CTSC_CONTACT));
    }

    public static TaskContext prepareTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        return context;
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithRespondentAsAddressee();
        caseData.put(CostOrderCoRespondentLetterDataExtractor.CaseDataKeys.COSTS_CLAIM_GRANTED, LETTER_DATE);

        caseData.put(CORESPONDENT_FIRST_NAME, CO_RESPONDENT_FIRST_NAME);
        caseData.put(CORESPONDENT_LAST_NAME, CO_RESPONDENT_LAST_NAME);

        return caseData;
    }
}


