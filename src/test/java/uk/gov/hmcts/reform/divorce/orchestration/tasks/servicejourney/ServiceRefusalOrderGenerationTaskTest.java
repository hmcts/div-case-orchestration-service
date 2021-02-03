package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Before;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceApplicationRefusalOrder;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTaskTest;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_MY_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RECEIVED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RECEIVED_SERVICE_APPLICATION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_REFUSAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceApplicationTestUtil.getDocumentCollection;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskTestHelper.formatWithCurrentDateTime;

public abstract class ServiceRefusalOrderGenerationTaskTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    @Before
    public void setup() {
        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);
        when(ccdUtil.addNewDocumentToCollection(anyMap(), any(GeneratedDocumentInfo.class), eq(SERVICE_APPLICATION_DOCUMENTS)))
            .thenCallRealMethod();
    }

    public abstract ServiceRefusalOrderGenerationTask getTask();

    public Map<String, Object> executeShouldGenerateAFile() throws TaskException {
        Map<String, Object> caseData = buildCaseData();

        Map<String, Object> returnedCaseData = getTask().execute(contextWithToken(), caseData);

        runVerifications(
            returnedCaseData,
            getExpectedDocumentType(),
            getExpectedTemplateId(),
            buildServiceApplicationRefusalOrder()
        );

        return returnedCaseData;
    }

    private void runVerifications(Map<String, Object> returnedCaseData, String expectedDocumentType, String expectedTemplateId,
                                  DocmosisTemplateVars expectedDocmosisTemplateVars) {
        verifyDocumentAddedToCaseData(returnedCaseData, expectedDocumentType);
        verifyPdfDocumentGenerationCallIsCorrect(expectedTemplateId, expectedDocmosisTemplateVars);
    }

    protected abstract String getExpectedDocumentType();

    protected abstract String getExpectedTemplateId();

    private void verifyDocumentAddedToCaseData(Map<String, Object> returnedCaseData, String expectedDocumentType) {
        String expectedServiceFileName = getFileName();
        List<CollectionMember<Document>> serviceApplicationCollection = getDocumentCollection(returnedCaseData, SERVICE_APPLICATION_DOCUMENTS);
        assertThat(serviceApplicationCollection.size(), is(1));

        Document newDocument = serviceApplicationCollection.get(0).getValue();
        assertThat(newDocument.getDocumentType(), is(expectedDocumentType));
        assertThat(newDocument.getDocumentFileName(), is(expectedServiceFileName));
    }

    private String getFileName() {
        return formatWithCurrentDateTime(getTask().getDocumentType());
    }

    private DocmosisTemplateVars buildServiceApplicationRefusalOrder() {
        return ServiceApplicationRefusalOrder.serviceApplicationRefusalOrderBuilder()
            .ctscContactDetails(CTSC_CONTACT)
            .petitionerFullName(TEST_PETITIONER_FULL_NAME)
            .respondentFullName(TEST_RESPONDENT_FULL_NAME)
            .caseReference(TEST_CASE_ID)
            .serviceApplicationRefusalReason(TEST_MY_REASON)
            .receivedServiceApplicationDate(DateUtils.formatDateWithCustomerFacingFormat(TEST_RECEIVED_DATE))
            .documentIssuedOn(DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now()))
            .build();
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        caseData.put(RECEIVED_SERVICE_APPLICATION_DATE, TEST_RECEIVED_DATE);
        caseData.put(SERVICE_APPLICATION_REFUSAL_REASON, TEST_MY_REASON);

        return caseData;
    }
}
