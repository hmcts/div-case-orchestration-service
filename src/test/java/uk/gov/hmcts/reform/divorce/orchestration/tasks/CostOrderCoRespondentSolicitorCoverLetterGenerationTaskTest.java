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
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoRespondentCostOrderNotificationCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentSolicitorCoverLetterGenerationTask;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COST_ORDER_CO_RESPONDENT_SOLICITOR_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.CO_RESPONDENT_SOLICITORS_EXPECTED_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.createHearingDatesList;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentSolicitorCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentSolicitorCoverLetterGenerationTask.FileMetadata.TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

@RunWith(MockitoJUnitRunner.class)
public class CostOrderCoRespondentSolicitorCoverLetterGenerationTaskTest {

    private static final String PETITIONERS_FIRST_NAME = "Anna";
    private static final String PETITIONERS_LAST_NAME = "Nowak";
    private static final String RESPONDENTS_FIRST_NAME = "John";
    private static final String RESPONDENTS_LAST_NAME = "Wozniak";

    private static final String SOLICITOR_REF = "SolRef1234";
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
    private CostOrderCoRespondentSolicitorCoverLetterGenerationTask costOrderNotificationLetterGenerationTask;

    @Captor
    private ArgumentCaptor<List<GeneratedDocumentInfo>> newDocumentInfoListCaptor;

    private GeneratedDocumentInfo createdDoc;

    public static TaskContext prepareTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        return context;
    }

    @Before
    public void setup() {
        createdDoc = createDocument();
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
        when(pdfDocumentGenerationService.generatePdf(any(DocmosisTemplateVars.class), eq(TEMPLATE_ID), eq(AUTH_TOKEN))).thenReturn(createdDoc);
    }

    @Test
    public void getDocumentType() {
        String documentType = costOrderNotificationLetterGenerationTask.getDocumentType();

        assertThat(documentType, is(COST_ORDER_CO_RESPONDENT_SOLICITOR_LETTER_DOCUMENT_TYPE));
    }

    @Test
    public void executeShouldPopulateFieldInContextWhenCoRespondentIsRepresented() throws TaskException {
        TaskContext context = prepareTaskContext();

        Map<String, Object> caseData = buildCaseDataCoRespondentRepresented();
        costOrderNotificationLetterGenerationTask.execute(context, caseData);

        verify(ccdUtil).addNewDocumentsToCaseData(eq(caseData), newDocumentInfoListCaptor.capture());
        List<GeneratedDocumentInfo> newDocumentInfoList = newDocumentInfoListCaptor.getValue();
        assertThat(newDocumentInfoList, hasSize(1));
        GeneratedDocumentInfo generatedDocumentInfo = newDocumentInfoList.get(0);
        assertThat(generatedDocumentInfo.getDocumentType(), is(DOCUMENT_TYPE));
        assertThat(generatedDocumentInfo.getFileName(), is(createdDoc.getFileName()));

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        verifyPdfDocumentGenerationCallIsCorrectForCostOrderNotificationLetter();
    }

    private void verifyPdfDocumentGenerationCallIsCorrectForCostOrderNotificationLetter() {
        final ArgumentCaptor<CoRespondentCostOrderNotificationCoverLetter> costOrderNotificationCoverLetterArgumentCaptor =
            ArgumentCaptor.forClass(CoRespondentCostOrderNotificationCoverLetter.class);

        verify(pdfDocumentGenerationService, times(1))
            .generatePdf(costOrderNotificationCoverLetterArgumentCaptor.capture(),
                eq(CostOrderCoRespondentSolicitorCoverLetterGenerationTask.FileMetadata.TEMPLATE_ID),
                eq(AUTH_TOKEN));

        final CoRespondentCostOrderNotificationCoverLetter coRespondentCostOrderNotificationCoverLetter
            = costOrderNotificationCoverLetterArgumentCaptor.getValue();
        assertThat(coRespondentCostOrderNotificationCoverLetter.getPetitionerFullName(),
            is(PETITIONERS_FIRST_NAME + " " + PETITIONERS_LAST_NAME));
        assertThat(coRespondentCostOrderNotificationCoverLetter.getRespondentFullName(),
            is(RESPONDENTS_FIRST_NAME + " " + RESPONDENTS_LAST_NAME));
        assertThat(coRespondentCostOrderNotificationCoverLetter.getCaseReference(), is(CASE_ID));
        assertThat(coRespondentCostOrderNotificationCoverLetter.getLetterDate(), is(LETTER_DATE_EXPECTED));
        assertThat(coRespondentCostOrderNotificationCoverLetter.getCtscContactDetails(), is(CTSC_CONTACT));
        assertThat(coRespondentCostOrderNotificationCoverLetter.getSolicitorReference(), is(SOLICITOR_REF));
        assertThat(coRespondentCostOrderNotificationCoverLetter.getAddressee().getFormattedAddress(), is(CO_RESPONDENT_SOLICITOR_ADDRESS));
        assertThat(coRespondentCostOrderNotificationCoverLetter.getAddressee().getName(), is(CO_RESPONDENT_SOLICITORS_EXPECTED_NAME));
    }

    private Map<String, Object> buildCaseDataCoRespondentRepresented() {
        return buildCaseData(true);
    }

    private Map<String, Object> buildCaseData(boolean isCoRespondentRepresented) {
        Map<String, Object> caseData = isCoRespondentRepresented
            ? AddresseeDataExtractorTest.buildCaseDataWithCoRespondentSolicitorAsAddressee()
            : AddresseeDataExtractorTest.buildCaseDataWithCoRespondentAsAddressee();

        caseData.put(PETITIONER_FIRST_NAME, PETITIONERS_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, PETITIONERS_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, RESPONDENTS_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, RESPONDENTS_LAST_NAME);

        caseData.put(DATETIME_OF_HEARING_CCD_FIELD, createHearingDatesList());

        return caseData;
    }

    private GeneratedDocumentInfo createDocument() {
        return GeneratedDocumentInfo.builder()
            .fileName("myFile.pdf")
            .build();
    }
}
