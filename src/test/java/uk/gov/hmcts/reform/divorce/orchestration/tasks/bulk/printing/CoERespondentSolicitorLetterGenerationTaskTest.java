package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoERespondentSolicitorCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.COE_RESPONDENT_SOLICITOR_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.RESPONDENT_SOLICITORS_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.RESPONDENT_SOLICITORS_EXPECTED_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.RESPONDENT_SOLICITOR_REF;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.COSTS_CLAIM_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.CaseDataKeys.HEARING_DATE_TIME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.CONTACT_COURT_BY_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.HEARING_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.createHearingDatesList;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.LETTER_DATE_EXPECTED;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

public class CoERespondentSolicitorLetterGenerationTaskTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    private static final String IS_COSTS_CLAIM_GRANTED_STRING_VALUE = YES_VALUE;
    private static final boolean IS_COSTS_CLAIM_GRANTED_BOOL_VALUE = true;

    @InjectMocks
    private CoERespondentSolicitorLetterGenerationTask coERespondentSolicitorLetterGenerationTask;

    @Before
    public void setup() {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
    }

    @Test
    public void executeShouldPopulateFieldInContextWhenRespondentIsRepresented() throws TaskException {
        TaskContext context = contextWithToken();

        Map<String, Object> caseData = buildCaseDataRespondentRepresented();
        Map<String, Object> returnedCaseData = coERespondentSolicitorLetterGenerationTask.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        final CoERespondentSolicitorCoverLetter expectedTemplateVars = CoERespondentSolicitorCoverLetter.coERespondentSolicitorCoverLetterBuilder()
            .petitionerFullName(TEST_PETITIONER_FULL_NAME)
            .respondentFullName(TEST_RESPONDENT_FULL_NAME)
            .caseReference(TEST_CASE_ID)
            .letterDate(LETTER_DATE_EXPECTED)
            .ctscContactDetails(CTSC_CONTACT)
            .solicitorReference(RESPONDENT_SOLICITOR_REF)
            .hearingDate(HEARING_DATE_FORMATTED)
            .costClaimGranted(IS_COSTS_CLAIM_GRANTED_BOOL_VALUE)
            .deadlineToContactCourtBy(CONTACT_COURT_BY_DATE_FORMATTED)
            .addressee(Addressee.builder().name(RESPONDENT_SOLICITORS_EXPECTED_NAME).formattedAddress(RESPONDENT_SOLICITORS_ADDRESS).build())
            .build();
        runCommonVerifications(caseData,
            returnedCaseData,
            COE_RESPONDENT_SOLICITOR_LETTER_DOCUMENT_TYPE,
            CoERespondentSolicitorLetterGenerationTask.FileMetadata.TEMPLATE_ID,
            expectedTemplateVars);
    }

    private Map<String, Object> buildCaseDataRespondentRepresented() {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithRespondentSolicitor();

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);

        caseData.put(COSTS_CLAIM_GRANTED, IS_COSTS_CLAIM_GRANTED_STRING_VALUE);
        caseData.put(HEARING_DATE_TIME, createHearingDatesList());

        return caseData;
    }

}