package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CertificateOfEntitlementCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Gender;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.COURT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.HEARING_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.HEARING_DATE_TIME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.IS_COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.IS_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CertificateOfEntitlementLetterDataExtractor.CaseDataKeys.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CertificateOfEntitlementLetterGenerationTask.FileMetadata.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CertificateOfEntitlementLetterGenerationTask.FileMetadata.TEMPLATE_ID_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CertificateOfEntitlementLetterGenerationTask.FileMetadata.TEMPLATE_ID_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils.todaysDate;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

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

    private static final String CASE_ID = "12345678910";
    private static final String LETTER_DATE = formatDateWithCustomerFacingFormat(todaysDate());
    private static final String HEARING_DATE_VALUE = "2020-06-20";
    private static final String HEARING_DATE_FORMATTED = "20 June 2020";
    private static final List<Map<String, Object>> DATE_TIME_OF_HEARINGS = singletonList(singletonMap("value", ImmutableMap.of(
        HEARING_DATE, HEARING_DATE_VALUE
    )));
    private static final String PETITIONER_GENDER_VALUE = Gender.MALE.getValue();
    private static final String HUSBAND_OR_WIFE = "husband";
    private static final String COURT_NAME_VALUE = "The Family Court at Southampton" ;
    private static final String LIMIT_DATE_TO_CONTACT_COURT_FORMATTED = "6 June 2020";
    private static final String SOLICITOR_REF_VALUE = "solRef123";
    private static final String IS_COSTS_CLAIM_GRANTED_STRING_VALUE = YES_VALUE;
    private static final boolean IS_COSTS_CLAIM_GRANTED_BOOL_VALUE = true;
    private static final Addressee ADDRESSEE_RESPONDENT = getRespondentAddressee();
    private static final Addressee ADDRESSEE_SOLICITOR = getSolicitorAddressee();

    private static final CtscContactDetails CTSC_CONTACT = CtscContactDetails.builder().build();

    @Mock
    private CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;

    @Mock
    private PdfDocumentGenerationService pdfDocumentGenerationService;

    @Mock
    private CcdUtil ccdUtil;


    @InjectMocks
    private CertificateOfEntitlementLetterGenerationTask certificateOfEntitlementLetterGenerationTask;

    @Captor
    private ArgumentCaptor<List<GeneratedDocumentInfo>> newDocumentInfoListCaptor;

    private GeneratedDocumentInfo createdDoc;

    @Before
    public void setup() {
        createdDoc = createDocument();
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
        when(pdfDocumentGenerationService.generatePdf(any(DocmosisTemplateVars.class), eq(TEMPLATE_ID_RESPONDENT), eq(AUTH_TOKEN)))
            .thenReturn(createdDoc);
        when(pdfDocumentGenerationService.generatePdf(any(DocmosisTemplateVars.class), eq(TEMPLATE_ID_SOLICITOR), eq(AUTH_TOKEN)))
            .thenReturn(createdDoc);
    }

    @Test
    public void executeShouldPopulateFieldInContextForRespondent() throws TaskException {
        boolean isRespondentRepresented = false;
        executeShouldPopulateFieldInContext(isRespondentRepresented);
    }

    @Test
    public void executeShouldPopulateFieldInContextForRespondentSolicitor() throws TaskException {
        boolean isRespondentRepresented = true;
        executeShouldPopulateFieldInContext(isRespondentRepresented);

    }

    @Test
    public void getDocumentType() {
        String documentType = certificateOfEntitlementLetterGenerationTask.getDocumentType();

        assertThat(documentType, is(CERTIFICATE_OF_ENTITLEMENT_LETTER_DOCUMENT_TYPE));
    }

    private void executeShouldPopulateFieldInContext(boolean isRespondentRepresented) throws TaskException {
        TaskContext context = prepareTaskContext();

        Map<String, Object> caseData = buildCaseData(isRespondentRepresented);
        certificateOfEntitlementLetterGenerationTask.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        verify(ccdUtil).addNewDocumentsToCaseData(eq(caseData), newDocumentInfoListCaptor.capture());

        List<GeneratedDocumentInfo> newDocumentInfoList = newDocumentInfoListCaptor.getValue();
        assertThat(newDocumentInfoList, hasSize(1));
        GeneratedDocumentInfo generatedDocumentInfo = newDocumentInfoList.get(0);
        assertThat(generatedDocumentInfo.getDocumentType(), is(DOCUMENT_TYPE));
        assertThat(generatedDocumentInfo.getFileName(), is(createdDoc.getFileName()));

        verifyPdfDocumentGenerationCallIsCorrect(isRespondentRepresented);
    }

    private void verifyPdfDocumentGenerationCallIsCorrect(boolean isRespondentRepresented) {
        final ArgumentCaptor<CertificateOfEntitlementCoverLetter> certificateOfEntitlementLetterArgumentCaptor =
            ArgumentCaptor.forClass(CertificateOfEntitlementCoverLetter.class);
        String templateId = isRespondentRepresented ? TEMPLATE_ID_SOLICITOR : TEMPLATE_ID_RESPONDENT;
        verify(pdfDocumentGenerationService, times(1))
            .generatePdf(certificateOfEntitlementLetterArgumentCaptor.capture(), eq(templateId), eq(AUTH_TOKEN));

        final CertificateOfEntitlementCoverLetter certificateOfEntitlementCoverLetter = certificateOfEntitlementLetterArgumentCaptor.getValue();

        if (isRespondentRepresented) {
            assertThat(certificateOfEntitlementCoverLetter.getAddressee(), is(ADDRESSEE_SOLICITOR));
            assertThat(certificateOfEntitlementCoverLetter.getSolicitorReference(), is(SOLICITOR_REF_VALUE));
        } else {
            assertThat(certificateOfEntitlementCoverLetter.getHusbandOrWife(), is(HUSBAND_OR_WIFE));
            assertThat(certificateOfEntitlementCoverLetter.getCourtName(), is(COURT_NAME_VALUE));
            assertThat(certificateOfEntitlementCoverLetter.getAddressee(), is(ADDRESSEE_RESPONDENT));
        }
        assertThat(certificateOfEntitlementCoverLetter.getPetitionerFullName(), is(PETITIONERS_FIRST_NAME + " " + PETITIONERS_LAST_NAME));
        assertThat(certificateOfEntitlementCoverLetter.getRespondentFullName(), is(RESPONDENTS_FIRST_NAME + " " + RESPONDENTS_LAST_NAME));
        assertThat(certificateOfEntitlementCoverLetter.getCaseReference(), is(CASE_ID));
        assertThat(certificateOfEntitlementCoverLetter.getLetterDate(), is(LETTER_DATE));
        assertThat(certificateOfEntitlementCoverLetter.getCtscContactDetails(), is(CTSC_CONTACT));
        assertThat(certificateOfEntitlementCoverLetter.getHearingDate(), is(HEARING_DATE_FORMATTED));
        assertThat(certificateOfEntitlementCoverLetter.isCostClaimGranted(), is(IS_COSTS_CLAIM_GRANTED_BOOL_VALUE));
        assertThat(certificateOfEntitlementCoverLetter.getDeadlineToContactCourtBy(), is(LIMIT_DATE_TO_CONTACT_COURT_FORMATTED));
    }

    public static TaskContext prepareTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        return context;
    }

    private Map<String, Object> buildCaseData(boolean respondentHasSolicitor) {
        Map<String, Object> caseData = new HashMap<>();
        String respondentIsRepresented = respondentHasSolicitor ? YES_VALUE : NO_VALUE;

        caseData.put(PETITIONER_FIRST_NAME, PETITIONERS_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, PETITIONERS_LAST_NAME);
        caseData.put(PETITIONER_GENDER, PETITIONER_GENDER_VALUE);
        caseData.put(RESPONDENT_FIRST_NAME, RESPONDENTS_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, RESPONDENTS_LAST_NAME);
        caseData.put(RESPONDENT_ADDRESS, RESPONDENTS_ADDRESS);
        caseData.put(RESPONDENT_SOLICITOR_NAME, SOLICITORS_FIRST_NAME + " " + SOLICITORS_LAST_NAME);
        caseData.put(SOLICITOR_ADDRESS, SOLICITORS_ADDRESS);
        caseData.put(HEARING_DATE_TIME, DATE_TIME_OF_HEARINGS);
        caseData.put(COURT_NAME, COURT_NAME_VALUE);
        caseData.put(IS_COSTS_CLAIM_GRANTED, IS_COSTS_CLAIM_GRANTED_STRING_VALUE);
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

    private GeneratedDocumentInfo createDocument() {
        return GeneratedDocumentInfo.builder()
            .fileName("myFile.pdf")
            .build();
    }
}
