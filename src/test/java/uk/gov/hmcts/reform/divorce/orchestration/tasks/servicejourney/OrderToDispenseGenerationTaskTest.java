package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.OrderToDispense;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTaskTest;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.prepareTaskContext;

@RunWith(MockitoJUnitRunner.class)
public class OrderToDispenseGenerationTaskTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    public static final String TEST_RECEIVED_DATE = "2010-10-10";
    public static final String TEST_SERVICE_DECISION_DATE = "2010-10-11";
    public static final String TEST_DOCUMENT_ISSUED_ON = "2020-12-12";
    @InjectMocks
    private OrderToDispenseGenerationTask orderToDispenseGenerationTask;

    @Before
    public void setup() {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
    }

    @Test
    public void executeShouldGenerateAFile() throws TaskException {
        Map<String, Object> caseData = buildCaseData();

        OrderToDispense orderToDispense = OrderToDispense.orderToDispenseBuilder()
            .ctscContactDetails(CTSC_CONTACT)
            .petitionerFullName(TEST_PETITIONER_FULL_NAME)
            .respondentFullName(TEST_RESPONDENT_FULL_NAME)
            .caseReference(CASE_ID)
            .serviceApplicationDecisionDate(DateUtils.formatDateWithCustomerFacingFormat(TEST_SERVICE_DECISION_DATE))
            .receivedServiceApplicationDate(DateUtils.formatDateWithCustomerFacingFormat(TEST_RECEIVED_DATE))
            .documentIssuedOn(DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now()))
            .build();

        Map<String, Object> returnedCaseData = orderToDispenseGenerationTask.execute(prepareTaskContext(), caseData);

        runCommonVerifications(
            caseData,
            returnedCaseData,
            orderToDispenseGenerationTask.getDocumentType(),
            orderToDispenseGenerationTask.getTemplateId(),
            orderToDispense
        );

        assertNotNull(returnedCaseData);
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        caseData.put(CcdFields.RECEIVED_SERVICE_APPLICATION_DATE, TEST_RECEIVED_DATE);
        caseData.put(CcdFields.SERVICE_APPLICATION_DECISION_DATE, TEST_SERVICE_DECISION_DATE);

        return caseData;
    }
}
