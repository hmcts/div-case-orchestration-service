package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.BasicCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.LETTER_DATE_EXPECTED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.LETTER_DATE_FROM_CCD;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.PETITIONERS_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.PETITIONERS_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.RESPONDENTS_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.RESPONDENTS_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.prepareTaskContext;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DaGrantedLetterGenerationTask.FileMetadata.TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskTestHelper.createRandomGeneratedDocument;

@RunWith(MockitoJUnitRunner.class)
public class DaGrantedLetterGenerationTaskTest {

    @Mock
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Mock
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    @Mock
    private CcdUtil ccdUtil;

    @InjectMocks
    private DaGrantedLetterGenerationTask daGrantedLetterGenerationTask;

    @Captor
    private ArgumentCaptor<List<GeneratedDocumentInfo>> newDocumentInfoListCaptor;

    private GeneratedDocumentInfo createdDoc;

    @Before
    public void setup() {
        createdDoc = createRandomGeneratedDocument();
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
        when(pdfDocumentGenerationService.generatePdf(any(DocmosisTemplateVars.class), eq(TEMPLATE_ID), eq(AUTH_TOKEN))).thenReturn(createdDoc);
    }

    @Test
    public void executeShouldPopulateFieldInContext() throws TaskException {
        TaskContext context = prepareTaskContext();

        Map<String, Object> caseData = buildCaseDataRespondentNotRepresented();
        daGrantedLetterGenerationTask.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        verify(ccdUtil).addNewDocumentsToCaseData(eq(caseData), newDocumentInfoListCaptor.capture());
        List<GeneratedDocumentInfo> newDocumentInfoList = newDocumentInfoListCaptor.getValue();
        assertThat(newDocumentInfoList, hasSize(1));
        GeneratedDocumentInfo generatedDocumentInfo = newDocumentInfoList.get(0);
        assertThat(generatedDocumentInfo.getDocumentType(), is(DECREE_ABSOLUTE_GRANTED_LETTER_DOCUMENT_TYPE));
        assertThat(generatedDocumentInfo.getFileName(), is(createdDoc.getFileName()));
        verifyPdfDocumentGenerationCallIsCorrect();
    }

    @Test
    public void executeShouldPopulateFieldInContextWhenRespondentIsRepresented() throws TaskException {
        TaskContext context = prepareTaskContext();

        Map<String, Object> caseData = buildCaseDataRespondentRepresented();
        daGrantedLetterGenerationTask.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        verify(ccdUtil).addNewDocumentsToCaseData(eq(caseData), newDocumentInfoListCaptor.capture());
        List<GeneratedDocumentInfo> newDocumentInfoList = newDocumentInfoListCaptor.getValue();
        assertThat(newDocumentInfoList, hasSize(1));
        GeneratedDocumentInfo generatedDocumentInfo = newDocumentInfoList.get(0);
        assertThat(generatedDocumentInfo.getDocumentType(), is(DECREE_ABSOLUTE_GRANTED_LETTER_DOCUMENT_TYPE));
        assertThat(generatedDocumentInfo.getFileName(), is(createdDoc.getFileName()));
        verifyPdfDocumentGenerationCallIsCorrect();
    }

    @Test
    public void getDocumentType() {
        String documentType = daGrantedLetterGenerationTask.getDocumentType();

        assertThat(documentType, is(DECREE_ABSOLUTE_GRANTED_LETTER_DOCUMENT_TYPE));
    }

    private void verifyPdfDocumentGenerationCallIsCorrect() {
        final ArgumentCaptor<BasicCoverLetter> daGrantedLetterArgumentCaptor = ArgumentCaptor.forClass(BasicCoverLetter.class);
        verify(pdfDocumentGenerationService, times(1))
            .generatePdf(daGrantedLetterArgumentCaptor.capture(), eq(TEMPLATE_ID), eq(AUTH_TOKEN));

        final BasicCoverLetter daGrantedLetter = daGrantedLetterArgumentCaptor.getValue();
        assertThat(daGrantedLetter.getPetitionerFullName(), is(PETITIONERS_FIRST_NAME + " " + PETITIONERS_LAST_NAME));
        assertThat(daGrantedLetter.getRespondentFullName(), is(RESPONDENTS_FIRST_NAME + " " + RESPONDENTS_LAST_NAME));
        assertThat(daGrantedLetter.getCaseReference(), is(CASE_ID));
        assertThat(daGrantedLetter.getLetterDate(), is(LETTER_DATE_EXPECTED));
        assertThat(daGrantedLetter.getCtscContactDetails(), is(CTSC_CONTACT));
    }

    private Map<String, Object> buildCaseDataRespondentRepresented() {
        return buildCaseData(true);
    }

    private Map<String, Object> buildCaseDataRespondentNotRepresented() {
        return buildCaseData(false);
    }

    private Map<String, Object> buildCaseData(boolean isRespondentRepresented) {
        Map<String, Object> caseData = isRespondentRepresented
            ? AddresseeDataExtractorTest.buildCaseDataWithRespondentSolicitorAsAddressee()
            : AddresseeDataExtractorTest.buildCaseDataWithRespondentAsAddressee();
        caseData.put(DatesDataExtractor.CaseDataKeys.DA_GRANTED_DATE, LETTER_DATE_FROM_CCD);

        caseData.put(PETITIONER_FIRST_NAME, PETITIONERS_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, PETITIONERS_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, RESPONDENTS_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, RESPONDENTS_LAST_NAME);

        return caseData;
    }
}
