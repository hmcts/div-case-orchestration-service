package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoERespondentCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Gender;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.CourtLookupService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.RESPONDENTS_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COURT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.HEARING_DATE_TIME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.PETITIONER_GENDER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.CONTACT_COURT_BY_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.HEARING_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.createHearingDatesList;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.LETTER_DATE_EXPECTED;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

public class CoERespondentCoverLetterGenerationTaskTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    private static final String IS_COSTS_CLAIM_GRANTED_STRING_VALUE = YES_VALUE;
    private static final boolean IS_COSTS_CLAIM_GRANTED_BOOL_VALUE = true;
    private static final String PETITIONER_GENDER_VALUE = Gender.MALE.getValue();
    private static final String HUSBAND_OR_WIFE = "husband";
    private static final String COURT_ID = "southampton";
    private static final String COURT_NAME_VALUE = "The Family Court at Southampton";

    @Mock
    private CourtLookupService courtLookupService;

    @InjectMocks
    private CoERespondentCoverLetterGenerationTask coERespondentCoverLetterGenerationTask;

    @Before
    public void setup() throws CourtDetailsNotFound {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
        when(courtLookupService.getDnCourtByKey(eq(COURT_ID))).thenReturn(getCourt());
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void executeShouldThrowInvalidDataForTaskException() throws TaskException, CourtDetailsNotFound {
        String invalidCourt = "I don't exist!";
        Map<String, Object> caseData = buildCaseDataRespondent();
        caseData.put(COURT_NAME, invalidCourt);

        when(courtLookupService.getDnCourtByKey(eq(invalidCourt))).thenThrow(new CourtDetailsNotFound(invalidCourt));

        coERespondentCoverLetterGenerationTask.execute(contextWithToken(), caseData);
    }

    @Test
    public void executeShouldPopulateFieldInContextWhenRespondentIsNotRepresented() throws TaskException {
        TaskContext context = contextWithToken();

        Map<String, Object> caseData = buildCaseDataRespondent();
        Map<String, Object> returnedCaseData = coERespondentCoverLetterGenerationTask.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        final CoERespondentCoverLetter expectedDocmosisTemplateVars = CoERespondentCoverLetter.coERespondentCoverLetterBuilder()
            .petitionerFullName(TEST_PETITIONER_FULL_NAME)
            .respondentFullName(TEST_RESPONDENT_FULL_NAME)
            .caseReference(TEST_CASE_ID)
            .letterDate(LETTER_DATE_EXPECTED)
            .ctscContactDetails(CTSC_CONTACT)
            .courtName(COURT_NAME_VALUE)
            .husbandOrWife(HUSBAND_OR_WIFE)
            .hearingDate(HEARING_DATE_FORMATTED)
            .costClaimGranted(IS_COSTS_CLAIM_GRANTED_BOOL_VALUE)
            .deadlineToContactCourtBy(CONTACT_COURT_BY_DATE_FORMATTED)
            .addressee(Addressee.builder().name(TEST_RESPONDENT_FULL_NAME).formattedAddress(RESPONDENTS_ADDRESS).build())
            .build();
        runCommonVerifications(caseData,
            returnedCaseData,
            "coeRespondentLetter",
            "FL-DIV-LET-ENG-00360.docx",
            expectedDocmosisTemplateVars);
    }

    private Map<String, Object> buildCaseDataRespondent() {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithRespondent();

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);

        caseData.put(PETITIONER_GENDER, PETITIONER_GENDER_VALUE);
        caseData.put(COSTS_CLAIM_GRANTED, IS_COSTS_CLAIM_GRANTED_STRING_VALUE);
        caseData.put(HEARING_DATE_TIME, createHearingDatesList());
        caseData.put(COURT_NAME, COURT_ID);

        return caseData;
    }

    private static DnCourt getCourt() {
        DnCourt court = new DnCourt();
        court.setName(COURT_NAME_VALUE);
        return court;
    }
}
