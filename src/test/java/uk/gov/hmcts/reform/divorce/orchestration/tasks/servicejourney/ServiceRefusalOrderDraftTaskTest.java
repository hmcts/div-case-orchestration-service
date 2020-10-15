package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTaskTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_MY_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RECEIVED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RECEIVED_SERVICE_APPLICATION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_REFUSAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_REFUSAL_DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.CTSC_CONTACT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ServiceApplicationTestUtil.getDocumentCollection;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;

public abstract class ServiceRefusalOrderDraftTaskTest extends BasePayloadSpecificDocumentGenerationTaskTest {

    protected abstract ServiceRefusalOrderDraftTask getTask();

    protected abstract String getTemplateId();

    protected abstract String documentType();

    protected abstract String getApplicationType();

    protected void shouldGenerateAndAddDraftDocument() {
        Map<String, Object> caseData = setUpFixturesForDraftAndReturnTestDataWith(getApplicationType());

        TaskContext context = contextWithToken();
        Map<String, Object> returnedCaseData = getTask().execute(context, caseData);

        runCommonDraftDocumentAssertions(returnedCaseData, getCaseId(context));
        runCommonDraftDocumentVerifications();
    }

    private Map<String, Object> setUpFixturesForDraftAndReturnTestDataWith(String serviceType) {
        Map<String, Object> caseData = buildRefusalOrderData(serviceType);

        when(ctscContactDetailsDataProviderService.getCtscContactDetails()).thenReturn(CTSC_CONTACT);

        return caseData;
    }

    private Map<String, Object> buildRefusalOrderData(String serviceType) {
        List<CollectionMember<Document>> documentCollection = new ArrayList<>();
        DocumentLink documentLink = getDocumentLink();

        Map<String, Object> payload = new HashMap<>();
        payload.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        payload.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        payload.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        payload.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        payload.put(RECEIVED_SERVICE_APPLICATION_DATE, TEST_RECEIVED_DATE);
        payload.put(SERVICE_APPLICATION_REFUSAL_REASON, TEST_MY_REASON);

        payload.put(SERVICE_APPLICATION_GRANTED, NO_VALUE);
        payload.put(SERVICE_APPLICATION_TYPE, serviceType);
        payload.put(SERVICE_REFUSAL_DRAFT, documentLink);

        payload.put(SERVICE_APPLICATION_DOCUMENTS, documentCollection);

        return payload;
    }

    private void runCommonDraftDocumentVerifications() {
        verify(ctscContactDetailsDataProviderService).getCtscContactDetails();
    }

    private void runCommonDraftDocumentAssertions(Map<String, Object> returnedCaseData, String caseId) {
        assertThat(returnedCaseData, notNullValue());
        assertThat(returnedCaseData, hasKey(SERVICE_REFUSAL_DRAFT));
        assertThat(getDocumentCollection(returnedCaseData, SERVICE_APPLICATION_DOCUMENTS), hasSize(0));

        DocumentLink documentLink = (DocumentLink) returnedCaseData.get(SERVICE_REFUSAL_DRAFT);
        assertThat(documentLink.getDocumentFilename(), containsString(".pdf"));
        assertThat(documentLink.getDocumentFilename(), containsString(caseId));
        assertThat(documentLink.getDocumentBinaryUrl(), containsString("binary"));
        assertThat(documentLink.getDocumentUrl(), notNullValue());
    }


    public static DocumentLink getDocumentLink() {
        DocumentLink documentLink = new DocumentLink();
        documentLink.setDocumentBinaryUrl("binary_url");
        documentLink.setDocumentFilename("file_name");
        documentLink.setDocumentUrl("url");

        return documentLink;
    }
}
