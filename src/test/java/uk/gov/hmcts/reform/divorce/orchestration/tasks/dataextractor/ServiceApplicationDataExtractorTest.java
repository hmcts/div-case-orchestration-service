package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.core.Is;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_MY_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_REFUSAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DEEMED;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.ServiceApplicationDataExtractor.CaseDataKeys.SERVICE_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.ServiceApplicationDataExtractor.getListOfServiceApplications;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.ServiceApplicationDataExtractor.getServiceApplicationGranted;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.ServiceApplicationDataExtractor.getServiceApplicationPayment;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.ServiceApplicationDataExtractor.getServiceApplicationRefusalReason;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.ServiceApplicationDataExtractor.getServiceApplicationType;

public class ServiceApplicationDataExtractorTest {

    @Test
    public void getServiceApplicationRefusalReasonShouldReturnValidValue() {
        Map<String, Object> caseData = buildCaseDataWithField(
            SERVICE_APPLICATION_REFUSAL_REASON,
            TEST_MY_REASON
        );
        assertThat(getServiceApplicationRefusalReason(caseData), is(TEST_MY_REASON));
    }

    @Test
    public void getServiceApplicationGrantedShouldReturnYesValue() {
        Map<String, Object> caseData = buildCaseDataWithField(
            SERVICE_APPLICATION_GRANTED,
            YES_VALUE);
        assertThat(getServiceApplicationGranted(caseData), is(YES_VALUE));
    }

    @Test
    public void getServiceApplicationTypeShouldReturnDeemedValue() {
        Map<String, Object> caseData = buildCaseDataWithField(SERVICE_APPLICATION_TYPE, DEEMED);
        assertThat(getServiceApplicationType(caseData), is(DEEMED));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getServiceApplicationRefusalReasonShouldThrowInvalidData() {
        getServiceApplicationRefusalReason(emptyMap());
    }

    @Test
    public void getServiceApplicationPaymentShouldReturnValue() {
        String paymentType = "feeAccount";

        Map<String, Object> caseData = buildCaseDataWithField(SERVICE_APPLICATION_PAYMENT, paymentType);

        assertThat(getServiceApplicationPayment(caseData), is(paymentType));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getServiceApplicationPaymentShouldThrowInvalidDataForTaskException() {
        getServiceApplicationPayment(emptyMap());
    }

    @Test
    public void givenNoField_whenGetListOfServiceApplications_shouldReturnAnEmptyArray() {
        List<CollectionMember<DivorceServiceApplication>> result = getListOfServiceApplications(emptyMap());

        assertThat(result, Is.is(empty()));
    }

    @Test
    public void givenFieldWithAnEmptyArray_whenGetListOfServiceApplications_shouldReturnEmptyArray() {
        final List<CollectionMember<DivorceServiceApplication>> myList = emptyList();

        List<CollectionMember<DivorceServiceApplication>> result =
            getListOfServiceApplications(ImmutableMap.of(CcdFields.SERVICE_APPLICATIONS, myList));

        assertThat(result, Is.is(empty()));
    }

    @Test
    public void givenFieldWithPopulatedArray_whenGetListOfServiceApplications_shouldReturnPopulatedArray() {
        final List<CollectionMember<DivorceServiceApplication>> myList = asList(new CollectionMember<>());

        List<CollectionMember<DivorceServiceApplication>> result =
            getListOfServiceApplications(ImmutableMap.of(CcdFields.SERVICE_APPLICATIONS, myList));

        assertThat(result.size(), Is.is(1));
        assertThat(result, Is.is(myList));
    }

    private static Map<String, Object> buildCaseDataWithField(String field, String value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(field, value);

        return caseData;
    }
}
