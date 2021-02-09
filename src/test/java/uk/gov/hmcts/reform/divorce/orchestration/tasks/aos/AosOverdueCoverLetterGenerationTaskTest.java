package uk.gov.hmcts.reform.divorce.orchestration.tasks.aos;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.AosOverdueCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTaskTest;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.PETITIONERS_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.PETITIONERS_HOME_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractorTest.buildCaseDataWithPetitionerHomeAddressButNoCorrespondenceAddress;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.formatCaseIdToReferenceNumber;

public class AosOverdueCoverLetterGenerationTaskTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    private static final String TEST_HELP_WITH_FEES_NUMBER = "HWF12345";

    private TaskContext context;

    @InjectMocks
    private AosOverdueCoverLetterGenerationTask classUnderTest;

    @Before
    public void setup() {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
        context = contextWithToken();
    }

    @Test
    public void shouldGenerateDocumentAndReturnItInCaseData_WhenHelpWithFeesIsPresent() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithHelpWithFeesWithNoCorrespondenceAddress();

        Map<String, Object> returnedCaseData = classUnderTest.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        final AosOverdueCoverLetter expectedDocmosisTemplateVars = AosOverdueCoverLetter.aosOverdueCoverLetterBuilder()
            .caseReference(formatCaseIdToReferenceNumber(TEST_CASE_ID))
            .addressee(Addressee.builder().name(TEST_PETITIONER_FULL_NAME).formattedAddress(PETITIONERS_HOME_ADDRESS).build())
            .ctscContactDetails(CTSC_CONTACT)
            .helpWithFeesNumber(TEST_HELP_WITH_FEES_NUMBER)
            .build();
        assertThat(expectedDocmosisTemplateVars.isHasHelpWithFeesNumber(), is(true));
        runCommonVerifications(caseData,
            returnedCaseData,
            "aosOverdueCoverLetter",
            "FL-DIV-LET-ENG-00537.odt",
            expectedDocmosisTemplateVars
        );
    }

    @Test
    public void shouldGenerateDocumentAndReturnItInCaseData_WhenHelpWithFeesIsNotPresent() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithNoHelpWithFeesWithCorrespondenceAddress();

        Map<String, Object> returnedCaseData = classUnderTest.execute(context, caseData);

        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
        final AosOverdueCoverLetter expectedDocmosisTemplateVars = AosOverdueCoverLetter.aosOverdueCoverLetterBuilder()
            .caseReference(formatCaseIdToReferenceNumber(TEST_CASE_ID))
            .addressee(Addressee.builder().name(TEST_PETITIONER_FULL_NAME).formattedAddress(PETITIONERS_CORRESPONDENCE_ADDRESS).build())
            .ctscContactDetails(CTSC_CONTACT)
            .build();
        assertThat(expectedDocmosisTemplateVars.isHasHelpWithFeesNumber(), is(false));
        runCommonVerifications(caseData,
            returnedCaseData,
            "aosOverdueCoverLetter",
            "FL-DIV-LET-ENG-00537.odt",
            expectedDocmosisTemplateVars
        );
    }

    private Map<String, Object> buildCaseDataWithHelpWithFeesWithNoCorrespondenceAddress() {
        Map<String, Object> caseData = buildCaseDataWithPetitionerHomeAddressButNoCorrespondenceAddress();

        caseData.put("D8HelpWithFeesReferenceNumber", TEST_HELP_WITH_FEES_NUMBER);

        return caseData;
    }

    private Map<String, Object> buildCaseDataWithNoHelpWithFeesWithCorrespondenceAddress() {
        return AddresseeDataExtractorTest.buildCaseDataWithPetitionerCorrespondenceAddressButNoHomeAddress();
    }

}