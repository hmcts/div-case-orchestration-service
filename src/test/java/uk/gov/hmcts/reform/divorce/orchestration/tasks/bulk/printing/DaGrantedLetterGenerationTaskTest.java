package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.BasicCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENTS_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.RESPONDENTS_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.RESPONDENT_SOLICITORS_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.RESPONDENT_SOLICITORS_EXPECTED_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.LETTER_DATE_EXPECTED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.LETTER_DATE_FROM_CCD;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.PETITIONERS_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.PETITIONERS_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.prepareTaskContext;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DaGrantedLetterGenerationTask.FileMetadata.TEMPLATE_ID;

public class DaGrantedLetterGenerationTaskTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    @InjectMocks
    private DaGrantedLetterGenerationTask daGrantedLetterGenerationTask;

    @Before
    public void setup() {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
    }

    @Test
    public void executeShouldPopulateFieldInContext() throws TaskException {
        TaskContext context = prepareTaskContext();

        Map<String, Object> caseData = buildCaseDataRespondentNotRepresented();
        Map<String, Object> returnedCaseData = daGrantedLetterGenerationTask.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        final DocmosisTemplateVars expectedDocmosisTemplateVars = BasicCoverLetter.builder()
            .petitionerFullName(PETITIONERS_FIRST_NAME + " " + PETITIONERS_LAST_NAME)
            .respondentFullName(TEST_RESPONDENTS_FULL_NAME)
            .caseReference(CASE_ID)
            .letterDate(LETTER_DATE_EXPECTED)
            .ctscContactDetails(CTSC_CONTACT)
            .addressee(Addressee.builder().name(TEST_RESPONDENTS_FULL_NAME).formattedAddress(RESPONDENTS_ADDRESS).build())
            .build();
        runCommonVerifications(caseData, returnedCaseData, DECREE_ABSOLUTE_GRANTED_LETTER_DOCUMENT_TYPE, TEMPLATE_ID, expectedDocmosisTemplateVars);
    }

    @Test
    public void executeShouldPopulateFieldInContextWhenRespondentIsRepresented() throws TaskException {
        TaskContext context = prepareTaskContext();

        Map<String, Object> caseData = buildCaseDataRespondentRepresented();
        Map<String, Object> returnedCaseData = daGrantedLetterGenerationTask.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        final DocmosisTemplateVars expectedDocmosisTemplateVars = BasicCoverLetter.builder()
            .petitionerFullName(PETITIONERS_FIRST_NAME + " " + PETITIONERS_LAST_NAME)
            .respondentFullName(TEST_RESPONDENTS_FULL_NAME)
            .caseReference(CASE_ID)
            .letterDate(LETTER_DATE_EXPECTED)
            .ctscContactDetails(CTSC_CONTACT)
            .addressee(Addressee.builder().name(RESPONDENT_SOLICITORS_EXPECTED_NAME).formattedAddress(RESPONDENT_SOLICITORS_ADDRESS).build())
            .build();
        runCommonVerifications(caseData, returnedCaseData, DECREE_ABSOLUTE_GRANTED_LETTER_DOCUMENT_TYPE, TEMPLATE_ID, expectedDocmosisTemplateVars);
    }

    private Map<String, Object> buildCaseDataRespondentRepresented() {
        return buildCaseData(true);
    }

    private Map<String, Object> buildCaseDataRespondentNotRepresented() {
        return buildCaseData(false);
    }

    private Map<String, Object> buildCaseData(boolean isRespondentRepresented) {
        Map<String, Object> caseData = isRespondentRepresented
            ? AddresseeDataExtractorTest.buildCaseDataWithRespondentSolicitor()
            : AddresseeDataExtractorTest.buildCaseDataWithRespondentWithAddress();
        caseData.put(DatesDataExtractor.CaseDataKeys.DA_GRANTED_DATE, LETTER_DATE_FROM_CCD);

        caseData.put(PETITIONER_FIRST_NAME, PETITIONERS_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, PETITIONERS_LAST_NAME);

        return caseData;
    }

}