package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoRespondentCostOrderCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.CO_RESPONDENTS_EXPECTED_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.CO_RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.HEARING_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.createHearingDatesList;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.PETITIONERS_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.PETITIONERS_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.RESPONDENTS_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.RESPONDENTS_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.createDocument;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.prepareTaskContext;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentCoverLetterGenerationTask.FileMetadata.TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

@RunWith(MockitoJUnitRunner.class)
public class CostOrderCoRespondentCoverLetterGenerationTaskTest {

    private static final String CO_RESPONDENTS_FIRST_NAME = "Jane";
    private static final String CO_RESPONDENTS_LAST_NAME = "Sam";

    private static final String CASE_ID = "It's mandatory field in context";
    private static final String LETTER_DATE_EXPECTED = formatDateWithCustomerFacingFormat(LocalDate.now());

    private static final CtscContactDetails CTSC_CONTACT = CtscContactDetails.builder().build();

    @Mock
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Mock
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    @Mock
    private CcdUtil ccdUtil;

    @InjectMocks
    private CostOrderCoRespondentCoverLetterGenerationTask costOrderCoRespondentCoverLetterGenerationTask;

    @Captor
    private ArgumentCaptor<List<GeneratedDocumentInfo>> newDocumentInfoListCaptor;

    private GeneratedDocumentInfo createdDoc;

    @Before
    public void setup() {
        createdDoc = createDocument();
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
        when(pdfDocumentGenerationService.generatePdf(any(DocmosisTemplateVars.class), eq(TEMPLATE_ID), eq(AUTH_TOKEN)))
            .thenReturn(createdDoc);
    }

    @Test
    public void getDocumentType() {
        String documentType = costOrderCoRespondentCoverLetterGenerationTask.getDocumentType();

        assertThat(documentType, is(DOCUMENT_TYPE));
    }

    @Test
    public void executeShouldPopulateFieldInContextWhenCoRespondentIsNotRepresented() throws TaskException {
        TaskContext context = prepareTaskContext();

        Map<String, Object> caseData = buildCaseDataWhenCoRespondentNotRepresented();
        costOrderCoRespondentCoverLetterGenerationTask.execute(context, caseData);

        verify(ccdUtil).addNewDocumentsToCaseData(eq(caseData), newDocumentInfoListCaptor.capture());
        List<GeneratedDocumentInfo> newDocumentInfoList = newDocumentInfoListCaptor.getValue();

        assertThat(newDocumentInfoList, hasSize(1));
        GeneratedDocumentInfo generatedDocumentInfo = newDocumentInfoList.get(0);
        assertThat(generatedDocumentInfo.getDocumentType(), is(DOCUMENT_TYPE));
        assertThat(generatedDocumentInfo.getFileName(), is(createdDoc.getFileName()));

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        verifyPdfDocumentGenerationCallIsCorrectForCostOrderLetter();
    }

    private void verifyPdfDocumentGenerationCallIsCorrectForCostOrderLetter() {
        final ArgumentCaptor<CoRespondentCostOrderCoverLetter> costOrderCoRespondentLetterArgumentCaptor =
            ArgumentCaptor.forClass(CoRespondentCostOrderCoverLetter.class);

        verify(pdfDocumentGenerationService, times(1))
            .generatePdf(
                costOrderCoRespondentLetterArgumentCaptor.capture(),
                eq(CostOrderCoRespondentCoverLetterGenerationTask.FileMetadata.TEMPLATE_ID),
                eq(AUTH_TOKEN)
            );

        final CoRespondentCostOrderCoverLetter coRespondentCoverLetter = costOrderCoRespondentLetterArgumentCaptor.getValue();
        assertThat(coRespondentCoverLetter.getPetitionerFullName(), is(PETITIONERS_FIRST_NAME + " " + PETITIONERS_LAST_NAME));
        assertThat(coRespondentCoverLetter.getRespondentFullName(), is(RESPONDENTS_FIRST_NAME + " " + RESPONDENTS_LAST_NAME));
        assertThat(coRespondentCoverLetter.getCaseReference(), is(CASE_ID));
        assertThat(coRespondentCoverLetter.getLetterDate(), is(LETTER_DATE_EXPECTED));
        assertThat(coRespondentCoverLetter.getCtscContactDetails(), is(CTSC_CONTACT));
        assertThat(coRespondentCoverLetter.getHearingDate(), is(HEARING_DATE_FORMATTED));
        assertThat(coRespondentCoverLetter.getAddressee().getFormattedAddress(), is(CO_RESPONDENT_ADDRESS));
        assertThat(coRespondentCoverLetter.getAddressee().getName(), is(CO_RESPONDENTS_EXPECTED_NAME));
    }

    private Map<String, Object> buildCaseDataWhenCoRespondentNotRepresented() {
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
        caseData.put(CO_RESPONDENTS_FIRST_NAME, CO_RESPONDENTS_FIRST_NAME);
        caseData.put(CO_RESPONDENTS_LAST_NAME, CO_RESPONDENTS_LAST_NAME);

        caseData.put(DATETIME_OF_HEARING_CCD_FIELD, createHearingDatesList());

        return caseData;
    }
}
