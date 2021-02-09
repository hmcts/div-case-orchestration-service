package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DaGrantedRespondentSolicitorCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.RESPONDENT_SOLICITORS_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.RESPONDENT_SOLICITORS_EXPECTED_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.RESPONDENT_SOLICITOR_REF;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.LETTER_DATE_EXPECTED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.LETTER_DATE_FROM_CCD;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

public class DaGrantedSolicitorLetterGenerationTaskTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    @InjectMocks
    private DaGrantedSolicitorLetterGenerationTask daGrantedLetterGenerationTask;

    @Before
    public void setup() {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
    }

    @Test
    public void executeShouldPopulateFieldInContextWhenRespondentIsRepresented() throws TaskException {
        TaskContext context = contextWithToken();

        Map<String, Object> caseData = buildCaseDataRespondentRepresented();
        Map<String, Object> returnedCaseData = daGrantedLetterGenerationTask.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        final DocmosisTemplateVars expectedTemplateVars = DaGrantedRespondentSolicitorCoverLetter.daGrantedRespondentSolicitorCoverLetterBuilder()
            .petitionerFullName(TEST_PETITIONER_FULL_NAME)
            .respondentFullName(TEST_RESPONDENT_FULL_NAME)
            .caseReference(TEST_CASE_ID)
            .letterDate(LETTER_DATE_EXPECTED)
            .ctscContactDetails(CTSC_CONTACT)
            .addressee(Addressee.builder().name(RESPONDENT_SOLICITORS_EXPECTED_NAME).formattedAddress(RESPONDENT_SOLICITORS_ADDRESS).build())
            .recipientReference(RESPONDENT_SOLICITOR_REF)
            .build();
        runCommonVerifications(caseData,
            returnedCaseData,
            DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER_DOCUMENT_TYPE,
            DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER.getTemplateByLanguage(ENGLISH),
            expectedTemplateVars);
    }

    private Map<String, Object> buildCaseDataRespondentRepresented() {
        Map<String, Object> caseData = AddresseeDataExtractorTest.buildCaseDataWithRespondentSolicitor();

        caseData.put(DatesDataExtractor.CaseDataKeys.DA_GRANTED_DATE, LETTER_DATE_FROM_CCD);

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);

        return caseData;
    }

}