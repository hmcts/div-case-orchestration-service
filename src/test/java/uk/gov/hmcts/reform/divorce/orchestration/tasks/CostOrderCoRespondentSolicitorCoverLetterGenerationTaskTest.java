package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoRespondentCostOrderCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTaskTest;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentSolicitorCoverLetterGenerationTask;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.CO_RESPONDENT_SOLICITORS_EXPECTED_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.HEARING_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.createHearingDatesList;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.LETTER_DATE_EXPECTED;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

public class CostOrderCoRespondentSolicitorCoverLetterGenerationTaskTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    @InjectMocks
    private CostOrderCoRespondentSolicitorCoverLetterGenerationTask costOrderNotificationLetterGenerationTask;

    @Before
    public void setup() {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
    }

    @Test
    public void executeShouldPopulateFieldInContextWhenCoRespondentIsRepresented() throws TaskException {
        TaskContext context = contextWithToken();

        Map<String, Object> caseData = buildCaseDataCoRespondentRepresented();
        Map<String, Object> returnedCaseData = costOrderNotificationLetterGenerationTask.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        final CoRespondentCostOrderCoverLetter expectedTemplateVars = CoRespondentCostOrderCoverLetter.coRespondentCostOrderCoverLetterBuilder()
            .petitionerFullName(TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME)
            .respondentFullName(TEST_RESPONDENT_FIRST_NAME + " " + TEST_RESPONDENT_LAST_NAME)
            .caseReference(TEST_CASE_ID)
            .letterDate(LETTER_DATE_EXPECTED)
            .ctscContactDetails(CTSC_CONTACT)
            .hearingDate(HEARING_DATE_FORMATTED)
            .addressee(Addressee.builder().name(CO_RESPONDENT_SOLICITORS_EXPECTED_NAME).formattedAddress(CO_RESPONDENT_SOLICITOR_ADDRESS).build())
            .build();
        runCommonVerifications(caseData,
            returnedCaseData,
            "coRespondentSolicitorCostOrderLetter",
            "FL-DIV-GNO-ENG-00423.docx",
            expectedTemplateVars);
    }

    private Map<String, Object> buildCaseDataCoRespondentRepresented() {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithCoRespondentSolicitor();

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        caseData.put(DATETIME_OF_HEARING_CCD_FIELD, createHearingDatesList());

        return caseData;
    }

}