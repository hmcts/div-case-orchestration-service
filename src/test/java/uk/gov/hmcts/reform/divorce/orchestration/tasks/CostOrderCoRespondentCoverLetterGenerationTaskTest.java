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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentCoverLetterGenerationTask;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENTS_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.CO_RESPONDENT_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.HEARING_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractorTest.createHearingDatesList;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.LETTER_DATE_EXPECTED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.PETITIONERS_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.PETITIONERS_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.RESPONDENTS_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.RESPONDENTS_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.prepareTaskContext;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentCoverLetterGenerationTask.FileMetadata.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentCoverLetterGenerationTask.FileMetadata.TEMPLATE_ID;

public class CostOrderCoRespondentCoverLetterGenerationTaskTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    @InjectMocks
    private CostOrderCoRespondentCoverLetterGenerationTask costOrderCoRespondentCoverLetterGenerationTask;

    @Before
    public void setup() {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
    }

    @Test
    public void executeShouldPopulateFieldInContextWhenCoRespondentIsNotRepresented() throws TaskException {
        TaskContext context = prepareTaskContext();

        Map<String, Object> caseData = buildCaseDataWhenCoRespondentNotRepresented();
        Map<String, Object> returnedCaseData = costOrderCoRespondentCoverLetterGenerationTask.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        final CoRespondentCostOrderCoverLetter coRespondentCoverLetter = CoRespondentCostOrderCoverLetter.builder()
            .petitionerFullName(PETITIONERS_FIRST_NAME + " " + PETITIONERS_LAST_NAME)
            .respondentFullName(RESPONDENTS_FIRST_NAME + " " + RESPONDENTS_LAST_NAME)
            .caseReference(CASE_ID)
            .letterDate(LETTER_DATE_EXPECTED)
            .ctscContactDetails(CTSC_CONTACT)
            .hearingDate(HEARING_DATE_FORMATTED)
            .addressee(Addressee.builder().name(TEST_CO_RESPONDENTS_FULL_NAME).formattedAddress(CO_RESPONDENT_ADDRESS).build())
            .build();
        runCommonVerifications(caseData, returnedCaseData, DOCUMENT_TYPE, TEMPLATE_ID, coRespondentCoverLetter);
    }

    private Map<String, Object> buildCaseDataWhenCoRespondentNotRepresented() {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithCoRespondent();

        caseData.put(PETITIONER_FIRST_NAME, PETITIONERS_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, PETITIONERS_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, RESPONDENTS_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, RESPONDENTS_LAST_NAME);

        caseData.put(DATETIME_OF_HEARING_CCD_FIELD, createHearingDatesList());

        return caseData;
    }

}