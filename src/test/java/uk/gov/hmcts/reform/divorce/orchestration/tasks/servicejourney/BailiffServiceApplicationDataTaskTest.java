package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.BailiffServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ADDED_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_FAMILY_MAN_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CERTIFICATE_OF_SERVICE_DATE;
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
public class BailiffServiceApplicationDataTaskTest {

    public static final String TEST_REASON_FAILURE_TO_SERVE = "reason failure to serve";
    public static final String TEST_COURT_EMAIL = "cour@example.com";
    public static final String TEST_COURT_ADDRESS = "Court address";

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private BailiffServiceApplicationDataTask bailiffServiceApplicationDataTask;

    @Before
    public void setup() {
        // feature toggle is not for bailiff logic here
        when(featureToggleService.isFeatureEnabled(any())).thenReturn(true);
    }

    @Test
    public void shouldExecuteAndAddElementToNewCollection() {
        Map<String, Object> input = buildCaseData();
        int originalSize = input.size();

        Map<String, Object> output = bailiffServiceApplicationDataTask.execute(context(), input);

        List<CollectionMember<BailiffServiceApplication>> collectionMembers = (List) output.get(SERVICE_APPLICATIONS);
        BailiffServiceApplication serviceApplication = collectionMembers.get(0).getValue();

        assertLastServiceApplicationIsPersisted(output);
        assertThat(output.size(), is(originalSize + 3));
        assertServiceApplicationIsCorrect(serviceApplication);
    }

    @Test
    public void shouldExecuteAndAddAnotherElementToExistingCollection() {
        Map<String, Object> input = buildCaseData();
        List<CollectionMember<BailiffServiceApplication>> memberList = new ArrayList<>(asList(buildCollectionMember()));
        input.put(SERVICE_APPLICATIONS, memberList);

        Map<String, Object> output = bailiffServiceApplicationDataTask.execute(context(), input);

        List<CollectionMember<BailiffServiceApplication>> collectionMembers = (List) output.get(SERVICE_APPLICATIONS);

        assertThat(collectionMembers.size(), is(2));

        BailiffServiceApplication serviceApplication = collectionMembers.get(1).getValue();

        assertLastServiceApplicationIsPersisted(output);
        assertServiceApplicationIsCorrect(serviceApplication);
    }

    @Test
    public void shouldExecuteAndAddAnotherElementToExistingCollectionOfDivorceServiceApplications() {
        Map<String, Object> input = buildCaseData();
        input.put(SERVICE_APPLICATIONS, new ArrayList<>(
            asList(ServiceApplicationDataTaskTest.buildCollectionMember())
        ));

        assertExecutedCorrectlyAndAnotherElementAddedToExistingCollection(input);
    }

    private void assertLastServiceApplicationIsPersisted(Map<String, Object> caseData) {
        BailiffServiceApplication serviceApplication = (BailiffServiceApplication) caseData.get(LAST_SERVICE_APPLICATION);
        assertServiceApplicationIsCorrect(serviceApplication);
    }

    private void assertExecutedCorrectlyAndAnotherElementAddedToExistingCollection(Map<String, Object> input) {
        Map<String, Object> output = bailiffServiceApplicationDataTask.execute(context(), input);

        List<CollectionMember<BailiffServiceApplication>> collectionMembers =
            (List) output.get(SERVICE_APPLICATIONS);

        assertThat(collectionMembers.size(), is(2));

        BailiffServiceApplication serviceApplication = collectionMembers.get(1).getValue();

        assertLastServiceApplicationIsPersisted(output);
        assertServiceApplicationIsCorrect(serviceApplication);
    }

    private void assertServiceApplicationIsCorrect(BailiffServiceApplication bailiffServiceApplication) {
        assertThat(bailiffServiceApplication.getReceivedDate(), is(TEST_RECEIVED_DATE));
        assertThat(bailiffServiceApplication.getAddedDate(), is(TEST_ADDED_DATE));
        assertThat(bailiffServiceApplication.getDecisionDate(), is(TEST_DECISION_DATE));
        assertThat(bailiffServiceApplication.getApplicationGranted(), is(YES_VALUE));
        assertThat(bailiffServiceApplication.getBailiffApplicationGranted(), is(YES_VALUE));
        assertThat(bailiffServiceApplication.getRefusalReason(), is(TEST_MY_REASON));
        assertThat(bailiffServiceApplication.getPayment(), is(TEST_SERVICE_APPLICATION_PAYMENT));
        assertThat(bailiffServiceApplication.getType(), is(ApplicationServiceTypes.BAILIFF));

        assertThat(bailiffServiceApplication.getLocalCourtAddress(), is(TEST_COURT_ADDRESS));
        assertThat(bailiffServiceApplication.getLocalCourtEmail(), is(TEST_COURT_EMAIL));
        assertThat(bailiffServiceApplication.getCertificateOfServiceDate(), is(TEST_CERTIFICATE_OF_SERVICE_DATE));
        assertThat(bailiffServiceApplication.getSuccessfulServedByBailiff(), is(YES_VALUE));
        assertThat(bailiffServiceApplication.getReasonFailureToServe(), is(TEST_REASON_FAILURE_TO_SERVE));
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
        caseData.put(CcdFields.SERVICE_APPLICATION_TYPE, ApplicationServiceTypes.BAILIFF);
        caseData.put(CcdFields.SERVICE_APPLICATION_PAYMENT, TEST_SERVICE_APPLICATION_PAYMENT);
        caseData.put(CcdFields.SERVICE_APPLICATION_REFUSAL_REASON, TEST_MY_REASON);
        caseData.put(CcdFields.SERVICE_APPLICATION_GRANTED, YES_VALUE);
        caseData.put(CcdFields.BAILIFF_APPLICATION_GRANTED, YES_VALUE);

        caseData.put(CcdFields.LOCAL_COURT_ADDRESS, TEST_COURT_ADDRESS);
        caseData.put(CcdFields.LOCAL_COURT_EMAIL, TEST_COURT_EMAIL);
        caseData.put(CcdFields.CERTIFICATE_OF_SERVICE_DATE, TEST_CERTIFICATE_OF_SERVICE_DATE);
        caseData.put(CcdFields.BAILIFF_SERVICE_SUCCESSFUL, YES_VALUE);
        caseData.put(CcdFields.REASON_FAILURE_TO_SERVE, TEST_REASON_FAILURE_TO_SERVE);

        return caseData;
    }

    public static CollectionMember<BailiffServiceApplication> buildCollectionMember() {
        CollectionMember<BailiffServiceApplication> collectionMember = new CollectionMember<>();
        collectionMember.setValue(BailiffServiceApplication.bailiffServiceApplicationBuilder().build());

        return collectionMember;
    }
}
