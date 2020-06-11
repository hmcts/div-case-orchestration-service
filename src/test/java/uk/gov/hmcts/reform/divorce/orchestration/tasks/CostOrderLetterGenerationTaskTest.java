package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoRespondentCostOrderCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COST_ORDER_CO_RESPONDENT_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.CostOrderLetterGenerationTask.FileMetadata.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.CostOrderLetterGenerationTask.FileMetadata.TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

@RunWith(MockitoJUnitRunner.class)
public class CostOrderLetterGenerationTaskTest {

    private static final String PETITIONERS_FIRST_NAME = "Anna";
    private static final String PETITIONERS_LAST_NAME = "Nowak";
    private static final String RESPONDENTS_FIRST_NAME = "John";
    private static final String RESPONDENTS_LAST_NAME = "Wozniak";
    private static final String CO_RESPONDENTS_FIRST_NAME = "Jane";
    private static final String CO_RESPONDENTS_LAST_NAME = "Sam";

    private static final String CASE_ID = "It's mandatory field in context";
    private static final String LETTER_DATE_FROM_CCD = LocalDate.now().toString();
    private static final String LETTER_DATE_EXPECTED = formatDateWithCustomerFacingFormat(LocalDate.now());

    private static final CtscContactDetails CTSC_CONTACT = CtscContactDetails.builder().build();

    @Mock
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Mock
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    @InjectMocks
    private CostOrderLetterGenerationTask costOrderLetterGenerationTask;

    private GeneratedDocumentInfo createdDoc;

    @Before
    public void setup() {
        createdDoc = createDocument();
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
        when(pdfDocumentGenerationService.generatePdf(any(DocmosisTemplateVars.class), eq(TEMPLATE_ID), eq(AUTH_TOKEN))).thenReturn(createdDoc);
    }

    @Test
    public void getDocumentType() {
        String documentType = costOrderLetterGenerationTask.getDocumentType();

        assertThat(documentType, is(COST_ORDER_CO_RESPONDENT_LETTER_DOCUMENT_TYPE));
    }

    @Test
    public void executeShouldPopulateFieldInContext() throws TaskException {
        TaskContext context = prepareTaskContext();

        costOrderLetterGenerationTask.execute(context, buildCaseDataCoRespondentNotRepresented());

        Set<GeneratedDocumentInfo> documents = context.getTransientObject(DOCUMENT_COLLECTION);
        assertThat(documents.size(), is(1));
        GeneratedDocumentInfo generatedDocumentInfo = documents.stream().findFirst().get();
        assertThat(generatedDocumentInfo.getDocumentType(), is(DOCUMENT_TYPE));
        assertThat(generatedDocumentInfo.getFileName(), is(createdDoc.getFileName()));

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        verifyPdfDocumentGenerationCallIsCorrectForCostOrderLetter();
    }


    private void verifyPdfDocumentGenerationCallIsCorrectForCostOrderLetter() {
        final ArgumentCaptor<CoRespondentCostOrderCoverLetter> costOrderCoRespondentLetterArgumentCaptor =
            ArgumentCaptor.forClass(CoRespondentCostOrderCoverLetter.class);

        verify(pdfDocumentGenerationService, times(1))
            .generatePdf(costOrderCoRespondentLetterArgumentCaptor.capture(), eq(CostOrderLetterGenerationTask.FileMetadata.TEMPLATE_ID),
                eq(AUTH_TOKEN));

        final CoRespondentCostOrderCoverLetter coRespondentCoverLetter = costOrderCoRespondentLetterArgumentCaptor.getValue();
        assertThat(coRespondentCoverLetter.getPetitionerFullName(), is(PETITIONERS_FIRST_NAME + " " + PETITIONERS_LAST_NAME));
        assertThat(coRespondentCoverLetter.getRespondentFullName(), is(RESPONDENTS_FIRST_NAME + " " + RESPONDENTS_LAST_NAME));
        assertThat(coRespondentCoverLetter.getCoRespondentFullName(), is(CO_RESPONDENTS_FIRST_NAME + " " + CO_RESPONDENTS_LAST_NAME));
        assertThat(coRespondentCoverLetter.getCaseReference(), is(CASE_ID));
        assertThat(coRespondentCoverLetter.getLetterDate(), is(LETTER_DATE_EXPECTED));
        assertThat(coRespondentCoverLetter.getCtscContactDetails(), is(CTSC_CONTACT));
    }

    public static TaskContext prepareTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        return context;
    }

    private Map<String, Object> buildCaseDataCoRespondentNotRepresented() {
        return buildCaseData(false);
    }

    private Map<String, Object> buildCaseData(boolean isCoRespondentRepresented) {
        Map<String, Object> caseData = isCoRespondentRepresented
            ? AddresseeDataExtractorTest.buildCaseDataWithCoRespondentSolicitorAsAddressee()
            : AddresseeDataExtractorTest.buildCaseDataWithCoRespondentAsAddressee();

        caseData.put(PETITIONER_FIRST_NAME, PETITIONERS_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, PETITIONERS_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, RESPONDENTS_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, RESPONDENTS_LAST_NAME);

        return caseData;
    }

    private GeneratedDocumentInfo createDocument() {
        return GeneratedDocumentInfo.builder()
            .fileName("myFile.pdf")
            .build();
    }
}
