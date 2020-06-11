package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoECoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.CourtLookupService;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractorTest.CONTACT_COURT_BY_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractorTest.HEARING_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractorTest.createHearingDatesList;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.PETITIONERS_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.PETITIONERS_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.RESPONDENTS_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.RESPONDENTS_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.prepareTaskContext;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoECoRespondentCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoECoRespondentCoverLetterGenerationTask.FileMetadata.TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskTestHelper.createRandomGeneratedDocument;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

@RunWith(MockitoJUnitRunner.class)
public class CoECoRespondentCoverLetterGenerationTaskTest {

    public static final String COURT_LOCATION = "liverpool";
    public static final String COURT_NAME = "court name";

    @Mock
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Mock
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    @Mock
    private CourtLookupService courtLookupService;

    @Mock
    private CcdUtil ccdUtil;

    @InjectMocks
    private CoECoRespondentCoverLetterGenerationTask coECoRespondentCoverLetterGenerationTask;

    @Captor
    private ArgumentCaptor<List<GeneratedDocumentInfo>> newDocumentInfoListCaptor;

    private GeneratedDocumentInfo createdDoc;

    @Before
    public void setup() throws CourtDetailsNotFound {
        createdDoc = createRandomGeneratedDocument();

        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
        when(pdfDocumentGenerationService.generatePdf(any(DocmosisTemplateVars.class), eq(TEMPLATE_ID), eq(AUTH_TOKEN)))
            .thenReturn(createdDoc);
        when(courtLookupService.getDnCourtByKey(COURT_LOCATION)).thenReturn(createDnCourt());
    }

    private DnCourt createDnCourt() {
        DnCourt court = new DnCourt();
        court.setName(COURT_NAME);
        return court;
    }

    @Test
    public void executeShouldPopulateFieldInContext() throws TaskException {
        TaskContext context = prepareTaskContext();

        Map<String, Object> caseData = buildCaseDataCoRespondentNotRepresented();
        coECoRespondentCoverLetterGenerationTask.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        verify(ccdUtil).addNewDocumentsToCaseData(eq(caseData), newDocumentInfoListCaptor.capture());
        List<GeneratedDocumentInfo> newDocumentInfoList = newDocumentInfoListCaptor.getValue();
        assertThat(newDocumentInfoList, hasSize(1));
        GeneratedDocumentInfo generatedDocumentInfo = newDocumentInfoList.get(0);
        assertThat(generatedDocumentInfo.getDocumentType(), is(DOCUMENT_TYPE));
        assertThat(generatedDocumentInfo.getFileName(), is(createdDoc.getFileName()));
        verifyPdfDocumentGenerationCallIsCorrect();
    }

    @Test
    public void executeShouldPopulateFieldInContextWhenRespondentIsRepresented() throws TaskException {
        TaskContext context = prepareTaskContext();

        Map<String, Object> caseData = buildCaseDataCoRespondentRepresented();
        coECoRespondentCoverLetterGenerationTask.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        verify(ccdUtil).addNewDocumentsToCaseData(eq(caseData), newDocumentInfoListCaptor.capture());
        List<GeneratedDocumentInfo> newDocumentInfoList = newDocumentInfoListCaptor.getValue();
        assertThat(newDocumentInfoList, hasSize(1));
        GeneratedDocumentInfo generatedDocumentInfo = newDocumentInfoList.get(0);
        assertThat(generatedDocumentInfo.getDocumentType(), is(DOCUMENT_TYPE));
        assertThat(generatedDocumentInfo.getFileName(), is(createdDoc.getFileName()));
        verifyPdfDocumentGenerationCallIsCorrect();
    }

    @Test
    public void getDocumentType() {
        String documentType = coECoRespondentCoverLetterGenerationTask.getDocumentType();

        assertThat(documentType, is(DOCUMENT_TYPE));
    }

    private void verifyPdfDocumentGenerationCallIsCorrect() {
        final ArgumentCaptor<CoECoverLetter> coECoverLetterArgumentCaptor = ArgumentCaptor.forClass(CoECoverLetter.class);
        verify(pdfDocumentGenerationService, times(1))
            .generatePdf(coECoverLetterArgumentCaptor.capture(), eq(TEMPLATE_ID), eq(AUTH_TOKEN));

        final CoECoverLetter coECoverLetter = coECoverLetterArgumentCaptor.getValue();
        assertThat(coECoverLetter.getPetitionerFullName(), is(PETITIONERS_FIRST_NAME + " " + PETITIONERS_LAST_NAME));
        assertThat(coECoverLetter.getRespondentFullName(), is(RESPONDENTS_FIRST_NAME + " " + RESPONDENTS_LAST_NAME));
        assertThat(coECoverLetter.getCaseReference(), is(CASE_ID));
        assertThat(coECoverLetter.getLetterDate(), is(formatDateWithCustomerFacingFormat(LocalDate.now())));
        assertThat(coECoverLetter.getCtscContactDetails(), is(CTSC_CONTACT));
        assertThat(coECoverLetter.getCourtName(), is(COURT_NAME));
        assertThat(coECoverLetter.getHearingDate(), is(HEARING_DATE_FORMATTED));
        assertThat(coECoverLetter.getDeadlineToContactCourtBy(), is(CONTACT_COURT_BY_DATE_FORMATTED));
    }

    private Map<String, Object> buildCaseDataCoRespondentRepresented() {
        return buildCaseData(true);
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

        caseData.put(COURT_NAME_CCD_FIELD, COURT_LOCATION);
        caseData.put(DATETIME_OF_HEARING_CCD_FIELD, createHearingDatesList());

        return caseData;
    }
}
