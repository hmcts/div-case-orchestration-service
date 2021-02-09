package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ADDED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DECISION_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_MY_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RECEIVED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.LAST_SERVICE_APPLICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATIONS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.CaseDataKeys.CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

@RunWith(MockitoJUnitRunner.class)
public class ServiceApplicationDataTaskTest {

    @InjectMocks
    private ServiceApplicationDataTask serviceApplicationDataTask;

    @Test
    public void shouldExecuteAndAddElementToNewCollection() {
        Map<String, Object> input = buildCaseData();
        int originalSize = input.size();

        Map<String, Object> output = serviceApplicationDataTask.execute(context(), input);

        List<CollectionMember<DivorceServiceApplication>> collectionMembers = (List) output.get(SERVICE_APPLICATIONS);
        DivorceServiceApplication serviceApplication = collectionMembers.get(0).getValue();

        assertLastServiceApplicationIsPersisted(output);
        assertThat(output.size(), is(originalSize + 2));
        assertServiceApplicationIsCorrect(serviceApplication);
    }

    @Test
    public void shouldExecuteAndAddAnotherElementToExistingCollection() {
        Map<String, Object> input = buildCaseData();
        List<CollectionMember<DivorceServiceApplication>> memberList = new ArrayList<>(asList(buildCollectionMember()));
        input.put(SERVICE_APPLICATIONS, memberList);

        Map<String, Object> output = serviceApplicationDataTask.execute(context(), input);

        List<CollectionMember<DivorceServiceApplication>> collectionMembers = (List) output.get(SERVICE_APPLICATIONS);

        assertThat(collectionMembers.size(), is(2));

        DivorceServiceApplication serviceApplication = collectionMembers.get(1).getValue();

        assertLastServiceApplicationIsPersisted(output);
        assertServiceApplicationIsCorrect(serviceApplication);
    }

    private void assertLastServiceApplicationIsPersisted(Map<String, Object> caseData) {
        DivorceServiceApplication serviceApplication = (DivorceServiceApplication) caseData.get(LAST_SERVICE_APPLICATION);
        assertServiceApplicationIsCorrect(serviceApplication);
    }

    private void assertServiceApplicationIsCorrect(DivorceServiceApplication serviceApplication) {
        assertThat(serviceApplication.getReceivedDate(), is(TEST_RECEIVED_DATE));
        assertThat(serviceApplication.getAddedDate(), is(TEST_ADDED_DATE));
        assertThat(serviceApplication.getDecisionDate(), is(TEST_DECISION_DATE));
        assertThat(serviceApplication.getApplicationGranted(), is(YES_VALUE));
        assertThat(serviceApplication.getRefusalReason(), is(TEST_MY_REASON));
        assertThat(serviceApplication.getPayment(), is(TEST_SERVICE_APPLICATION_PAYMENT));
        assertThat(serviceApplication.getType(), is(ApplicationServiceTypes.DEEMED));
    }

    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(CASE_REFERENCE, TEST_CASE_FAMILY_MAN_ID);

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);

        caseData.put(CcdFields.RECEIVED_SERVICE_APPLICATION_DATE, TEST_RECEIVED_DATE);
        caseData.put(CcdFields.SERVICE_APPLICATION_DECISION_DATE, TEST_DECISION_DATE);
        caseData.put(CcdFields.RECEIVED_SERVICE_ADDED_DATE, TEST_ADDED_DATE);
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.DEEMED);
        caseData.put(CcdFields.SERVICE_APPLICATION_PAYMENT, TEST_SERVICE_APPLICATION_PAYMENT);
        caseData.put(CcdFields.SERVICE_APPLICATION_REFUSAL_REASON, TEST_MY_REASON);
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, YES_VALUE);

        return caseData;
    }

    public static CollectionMember<DivorceServiceApplication> buildCollectionMember() {
        CollectionMember<DivorceServiceApplication> collectionMember = new CollectionMember<>();
        collectionMember.setValue(DivorceServiceApplication.builder().build());

        return collectionMember;
    }
}
