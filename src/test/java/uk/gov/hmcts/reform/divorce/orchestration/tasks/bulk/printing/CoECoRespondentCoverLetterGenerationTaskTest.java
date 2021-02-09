package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoECoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.CourtLookupService;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.CO_RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.CO_RESPONDENT_SOLICITORS_EXPECTED_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.CONTACT_COURT_BY_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.HEARING_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.createHearingDatesList;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

public class CoECoRespondentCoverLetterGenerationTaskTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    public static final String COURT_LOCATION = "liverpool";
    public static final String COURT_NAME = "court name";

    @Mock
    private CourtLookupService courtLookupService;

    @InjectMocks
    private CoECoRespondentCoverLetterGenerationTask coECoRespondentCoverLetterGenerationTask;

    @Before
    public void setup() throws CourtDetailsNotFound {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
        when(courtLookupService.getDnCourtByKey(COURT_LOCATION)).thenReturn(createDnCourt());
    }

    @Test
    public void executeShouldPopulateFieldInContext() throws TaskException {
        TaskContext context = contextWithToken();

        Map<String, Object> caseData = buildCaseDataCoRespondentNotRepresented();
        Map<String, Object> returnedCaseData = coECoRespondentCoverLetterGenerationTask.execute(context, caseData);

        CoECoverLetter expectedDocmosisTemplateVars = CoECoverLetter.coECoverLetterBuilder()
            .petitionerFullName(TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME)
            .respondentFullName(TEST_RESPONDENT_FIRST_NAME + " " + TEST_RESPONDENT_LAST_NAME)
            .caseReference(TEST_CASE_ID)
            .letterDate(formatDateWithCustomerFacingFormat(LocalDate.now()))
            .ctscContactDetails(CTSC_CONTACT)
            .courtName(COURT_NAME)
            .hearingDate(HEARING_DATE_FORMATTED)
            .costClaimGranted(true)
            .deadlineToContactCourtBy(CONTACT_COURT_BY_DATE_FORMATTED)
            .addressee(
                Addressee.builder()
                    .name(TEST_CO_RESPONDENT_FULL_NAME)
                    .formattedAddress(CO_RESPONDENT_ADDRESS)
                    .build()
            ).build();

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        runCommonVerifications(
            caseData,
            returnedCaseData,
            "coeCoRespondentLetter",
            "FL-DIV-GNO-ENG-00449.docx",
            expectedDocmosisTemplateVars
        );
    }

    @Test
    public void executeShouldPopulateFieldInContextWhenRespondentIsRepresented() throws TaskException {
        TaskContext context = contextWithToken();

        Map<String, Object> caseData = buildCaseDataCoRespondentRepresented();
        Map<String, Object> returnedCaseData = coECoRespondentCoverLetterGenerationTask.execute(context, caseData);

        CoECoverLetter expectedDocmosisTemplateVars = CoECoverLetter.coECoverLetterBuilder()
            .petitionerFullName(TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME)
            .respondentFullName(TEST_RESPONDENT_FIRST_NAME + " " + TEST_RESPONDENT_LAST_NAME)
            .caseReference(TEST_CASE_ID)
            .letterDate(formatDateWithCustomerFacingFormat(LocalDate.now()))
            .ctscContactDetails(CTSC_CONTACT)
            .courtName(COURT_NAME)
            .costClaimGranted(true)
            .hearingDate(HEARING_DATE_FORMATTED)
            .deadlineToContactCourtBy(CONTACT_COURT_BY_DATE_FORMATTED)
            .addressee(Addressee.builder().name(CO_RESPONDENT_SOLICITORS_EXPECTED_NAME).formattedAddress(CO_RESPONDENT_SOLICITOR_ADDRESS).build())
            .build();
        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        runCommonVerifications(
            caseData,
            returnedCaseData,
            "coeCoRespondentLetter",
            "FL-DIV-GNO-ENG-00449.docx",
            expectedDocmosisTemplateVars
        );
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void executeShouldThrowCourtDetailsNotFound() throws TaskException, CourtDetailsNotFound {
        final String invalidCourtLocation = "you will not find this court";
        TaskContext context = contextWithToken();
        when(courtLookupService.getDnCourtByKey(invalidCourtLocation)).thenThrow(CourtDetailsNotFound.class);

        Map<String, Object> caseData = buildCaseDataCoRespondentNotRepresented();
        caseData.put(COURT_NAME_CCD_FIELD, invalidCourtLocation);

        coECoRespondentCoverLetterGenerationTask.execute(context, caseData);
    }

    private Map<String, Object> buildCaseDataCoRespondentRepresented() {
        return buildCaseData(true);
    }

    private Map<String, Object> buildCaseDataCoRespondentNotRepresented() {
        return buildCaseData(false);
    }

    private Map<String, Object> buildCaseData(boolean isCoRespondentRepresented) {
        Map<String, Object> caseData = isCoRespondentRepresented
            ? AddresseeDataExtractorTest.buildCaseDataWithCoRespondentSolicitor()
            : AddresseeDataExtractorTest.buildCaseDataWithCoRespondent();

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        caseData.put(COURT_NAME_CCD_FIELD, COURT_LOCATION);
        caseData.put(DATETIME_OF_HEARING_CCD_FIELD, createHearingDatesList());

        return caseData;
    }

    private DnCourt createDnCourt() {
        DnCourt court = new DnCourt();
        court.setName(COURT_NAME);
        return court;
    }

}