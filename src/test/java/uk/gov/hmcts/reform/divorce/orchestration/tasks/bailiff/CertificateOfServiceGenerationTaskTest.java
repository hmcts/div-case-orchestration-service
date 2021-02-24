package uk.gov.hmcts.reform.divorce.orchestration.tasks.bailiff;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.CertificateOfService;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTaskTest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.CO_RESPONDENT_LINKED_TO_CASE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.formatCaseIdToReferenceNumber;

public class CertificateOfServiceGenerationTaskTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    private TaskContext context;

    @InjectMocks
    private CertificateOfServiceGenerationTask certificateOfServiceGenerationTask;

    @Before
    public void setup() {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
        context = contextWithToken();
    }

    @Test
    public void shouldGenerateDocumentAndReturnItInCaseData_WhenHelpWithFeesIsPresent() throws TaskException {
        Map<String, Object> caseData = prepareCaseData();

        Map<String, Object> returnedCaseData = certificateOfServiceGenerationTask.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        final CertificateOfService expectedDocmosisTemplateVars = CertificateOfService.certificateOfServiceBuilder()
            .ctscContactDetails(CTSC_CONTACT)
            .caseReference(formatCaseIdToReferenceNumber(TEST_CASE_ID))
            .petitionerFullName(TEST_PETITIONER_FULL_NAME)
            .respondentFullName(TEST_RESPONDENT_FULL_NAME)
            .coRespondentFullName(TEST_CO_RESPONDENT_FULL_NAME)
            .hasCoRespondent(true)
            .build();
        runCommonVerifications(caseData,
            returnedCaseData,
            "certificateOfService",
            "FL-DIV-GNO-ENG-00595.docx",
            expectedDocmosisTemplateVars
        );
    }

    private Map<String, Object> prepareCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);
        caseData.put(CO_RESPONDENT_FIRST_NAME, TEST_CO_RESPONDENT_FIRST_NAME);
        caseData.put(CO_RESPONDENT_LAST_NAME, TEST_CO_RESPONDENT_LAST_NAME);
        caseData.put(CO_RESPONDENT_LINKED_TO_CASE, YES_VALUE);

        return caseData;
    }
}
