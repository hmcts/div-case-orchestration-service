package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CertificateOfEntitlementCoverLetter;
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
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.*;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.*;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CertificateOfEntitlementLetterGenerationTask.FileMetadata.TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.PrepareDataForDocumentGenerationTaskTest.document;

@RunWith(MockitoJUnitRunner.class)
public class CertificateOfEntitlementLetterGenerationTaskTest {

    private static final String PETITIONERS_FIRST_NAME = "petFirstname";
    private static final String PETITIONERS_LAST_NAME = "petLastname";
    private static final String RESPONDENTS_FIRST_NAME = "resFirstname";
    private static final String RESPONDENTS_LAST_NAME = "resLastName";
    private static final String SOLICITORS_FIRST_NAME = "solFirstname";
    private static final String SOLICITORS_LAST_NAME = "solLastName";
    private static final String RESPONDENTS_ADDRESS = "10 Respondent Street\nAnnex B1\nRespondentville\nRespondentshire\nB13 B34";
    private static final String SOLICITORS_ADDRESS = "10 Solicitor Street\nAnnex B1\nSolicitorville\nSolicitorshire\nB13 B33";

    private static final String CASE_ID = "It's mandatory field in context";
    private static final LocalDate today = LocalDate.now();
    private static final String LETTER_DATE = today.format(DateTimeFormatter.ofPattern("dd-MMM-yy"));

    private static final String HEARING_DATE_VALUE = "2020-06-20";
    private static final String HEARING_DATE_FORMATTED = "20 June 2020";
    private static final String HUSBAND_OR_WIFE = "husband";
    private static final String COURT_NAME_VALUE = "The Family Court at Southampton" ;
    private static final String LIMIT_DATE_TO_CONTACT_COURT_FORMATTED = "06 June 2020";
    private static final String SOLICITOR_REF_VALUE = "solRef123";
    private static final boolean IS_COSTS_CLAIM_GRANTED_VALUE = true;
    private static final Addressee ADDRESSEE_RESPONDENT = getRespondentAddressee();
    private static final Addressee ADDRESSEE_SOLICITOR = getSolicitorAddressee();

    private static final CtscContactDetails CTSC_CONTACT = CtscContactDetails.builder().build();
    private static final GeneratedDocumentInfo DOCUMENT = GeneratedDocumentInfo
        .builder()
        .documentType(CertificateOfEntitlementLetterGenerationTask.FileMetadata.DOCUMENT_TYPE)
        .fileName(CertificateOfEntitlementLetterGenerationTask.FileMetadata.FILE_NAME)
        .build();

    @Mock
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Mock
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    @Mock
    private DocumentContentFetcherService documentContentFetcherService;

    @InjectMocks
    private CertificateOfEntitlementLetterGenerationTask certificateOfEntitlementLetterGenerationTask;

    @Before
    public void setup() {
        GeneratedDocumentInfo createdDoc = document();
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
        when(pdfDocumentGenerationService.generatePdf(any(DocmosisTemplateVars.class), eq(TEMPLATE_ID), eq(AUTH_TOKEN)))
            .thenReturn(createdDoc);
        when(documentContentFetcherService.fetchPrintContent(createdDoc)).thenReturn(DOCUMENT);
    }

    @Test
    public void executeShouldPopulateFieldInContextForRespondent() throws TaskException {
        TaskContext context = prepareTaskContext();
        boolean respondentHasSolicitor = false;

        certificateOfEntitlementLetterGenerationTask.execute(context, buildCaseData(respondentHasSolicitor));

        Map<String, GeneratedDocumentInfo> documents = PrepareDataForDocumentGenerationTask.getDocumentsToBulkPrint(context);

        assertThat(documents.size(), is(1));
        assertThat(documents.get(DOCUMENT.getDocumentType()), is(DOCUMENT));
        verify(ctscContactDetailsDataProviderService, times(1)).getCtscContactDetails();
        verifyPdfDocumentGenerationCallIsCorrect(respondentHasSolicitor);
    }

    @Test
    public void executeShouldPopulateFieldInContextForRespondentSolicitor() throws TaskException {
        TaskContext context = prepareTaskContext();
        boolean respondentHasSolicitor = true;

        certificateOfEntitlementLetterGenerationTask.execute(context, buildCaseData(respondentHasSolicitor));

        Map<String, GeneratedDocumentInfo> documents = PrepareDataForDocumentGenerationTask.getDocumentsToBulkPrint(context);

        assertThat(documents.size(), is(1));
        assertThat(documents.get(DOCUMENT.getDocumentType()), is(DOCUMENT));
        verify(ctscContactDetailsDataProviderService, times(1)).getCtscContactDetails();
        verifyPdfDocumentGenerationCallIsCorrect(respondentHasSolicitor);
    }

    private void verifyPdfDocumentGenerationCallIsCorrect(boolean respondentHasSolicitor) {
        final ArgumentCaptor<CertificateOfEntitlementCoverLetter> certificateOfEntitlementLetterArgumentCaptor = ArgumentCaptor.forClass(CertificateOfEntitlementCoverLetter.class);
        verify(pdfDocumentGenerationService, times(1))
            .generatePdf(certificateOfEntitlementLetterArgumentCaptor.capture(), eq(TEMPLATE_ID), eq(AUTH_TOKEN));
        verify(documentContentFetcherService, times(1)).fetchPrintContent(eq(DOCUMENT));

        final CertificateOfEntitlementCoverLetter certificateOfEntitlementCoverLetter = certificateOfEntitlementLetterArgumentCaptor.getValue();

        if (respondentHasSolicitor) {
            assertThat(certificateOfEntitlementCoverLetter.getAddressee(), is(ADDRESSEE_SOLICITOR));
        } else {
            assertThat(certificateOfEntitlementCoverLetter.getHusbandOrWife(), is(HUSBAND_OR_WIFE));
            assertThat(certificateOfEntitlementCoverLetter.getCourtName(), is(COURT_NAME));
            assertThat(certificateOfEntitlementCoverLetter.getAddressee(), is(ADDRESSEE_RESPONDENT));
        }
        assertThat(certificateOfEntitlementCoverLetter.getPetitionerFullName(), is("petFirstname petLastname"));
        assertThat(certificateOfEntitlementCoverLetter.getRespondentFullName(), is("resFirstname resLastname"));
        assertThat(certificateOfEntitlementCoverLetter.getCaseReference(), is(CASE_ID));
        assertThat(certificateOfEntitlementCoverLetter.getLetterDate(), is(LETTER_DATE));
        assertThat(certificateOfEntitlementCoverLetter.getCtscContactDetails(), is(CTSC_CONTACT));
        assertThat(certificateOfEntitlementCoverLetter.getHearingDate(), is(HEARING_DATE_FORMATTED));
        assertThat(certificateOfEntitlementCoverLetter.isCostClaimGranted(), is(IS_COSTS_CLAIM_GRANTED_VALUE));
        assertThat(certificateOfEntitlementCoverLetter.getDeadlineToContactCourtBy(), is(LIMIT_DATE_TO_CONTACT_COURT_FORMATTED));
    }

    public static TaskContext prepareTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        return context;
    }

    private Map<String, Object> buildCaseData(boolean respondentHasSolicitor) {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithRespondentAsAddressee();
        String respondentIsRepresented = respondentHasSolicitor ? YES_VALUE : NO_VALUE;
        caseData.put(DaGrantedLetterDataExtractor.CaseDataKeys.DA_GRANTED_DATE, LETTER_DATE);

        caseData.put(PETITIONER_FIRST_NAME, PETITIONERS_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, PETITIONERS_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, RESPONDENTS_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, RESPONDENTS_LAST_NAME);
        caseData.put(RESPONDENT_ADDRESS, RESPONDENTS_ADDRESS);
        caseData.put(SOLICITOR_ADDRESS, SOLICITORS_ADDRESS);
        caseData.put(HEARING_DATE, HEARING_DATE_VALUE);
        caseData.put(COURT_NAME, COURT_NAME_VALUE);
        caseData.put(IS_COSTS_CLAIM_GRANTED, IS_COSTS_CLAIM_GRANTED_VALUE);
        caseData.put(SOLICITOR_REFERENCE, SOLICITOR_REF_VALUE);
        caseData.put(IS_RESPONDENT_REPRESENTED, respondentIsRepresented);

        return caseData;
    }

    private static Addressee getRespondentAddressee() {
        return Addressee.builder()
            .name(RESPONDENTS_FIRST_NAME + " " + RESPONDENTS_LAST_NAME)
            .formattedAddress(RESPONDENTS_ADDRESS)
            .build();
    }

    private static Addressee getSolicitorAddressee() {
        return Addressee.builder()
            .name(SOLICITORS_FIRST_NAME + " " + SOLICITORS_LAST_NAME)
            .formattedAddress(SOLICITORS_ADDRESS)
            .build();
    }
}
