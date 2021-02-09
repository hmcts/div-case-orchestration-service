package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.util.Strings;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceServiceApplication;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_MY_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_REFUSAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_APPLICATION_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DEEMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes.DISPENSED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.CaseDataKeys.REFUSAL_REASON;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.CaseDataKeys.SERVICE_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.getLastServiceApplication;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.getListOfServiceApplications;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.getServiceApplicationGranted;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.getServiceApplicationPayment;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.getServiceApplicationRefusalReason;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.getServiceApplicationRefusalReasonOrEmpty;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.ServiceApplicationDataExtractor.getServiceApplicationType;

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

        assertThat(result, is(empty()));
    }

    @Test
    public void givenFieldWithAnEmptyArray_whenGetListOfServiceApplications_shouldReturnEmptyArray() {
        final List<CollectionMember<DivorceServiceApplication>> myList = emptyList();

        List<CollectionMember<DivorceServiceApplication>> result =
            getListOfServiceApplications(ImmutableMap.of(CcdFields.SERVICE_APPLICATIONS, myList));

        assertThat(result, is(empty()));
    }

    @Test
    public void givenFieldWithPopulatedArray_whenGetListOfServiceApplications_shouldReturnPopulatedArray() {
        final List<CollectionMember<DivorceServiceApplication>> myList = asList(new CollectionMember<>());

        List<CollectionMember<DivorceServiceApplication>> result =
            getListOfServiceApplications(ImmutableMap.of(CcdFields.SERVICE_APPLICATIONS, myList));

        assertThat(result.size(), is(1));
        assertThat(result, is(myList));
    }

    @Test
    public void givenNoField_whenGetServiceApplicationRefusalReasonOrEmpty_shouldReturnEmptyString() {
        assertThat(getServiceApplicationRefusalReasonOrEmpty(new HashMap<>()), is(Strings.EMPTY));
    }

    @Test
    public void givenFieldPopulated_whenGetServiceApplicationRefusalReasonOrEmpty_shouldReturnValue() {
        String expectedValue = "value";

        assertThat(
            getServiceApplicationRefusalReasonOrEmpty(ImmutableMap.of(REFUSAL_REASON, expectedValue)),
            is(expectedValue)
        );
    }

    @Test
    public void getLastServiceApplicationShouldReturnEmptyWhenNoList() {
        DivorceServiceApplication result = getLastServiceApplication(new HashMap<>());

        assertNull(result.getType());
    }

    @Test
    public void getLastServiceApplicationShouldReturnEmptyWhenEmptyList() {
        DivorceServiceApplication result = getLastServiceApplication(
            ImmutableMap.of(CcdFields.SERVICE_APPLICATIONS, new ArrayList<>())
        );

        assertNull(result.getType());
    }

    @Test
    public void getLastServiceApplicationShouldReturnElementWhenOneElementOnList() {
        final List<CollectionMember<DivorceServiceApplication>> myList = new ArrayList<>();

        myList.add(buildCollectionMember(YES_VALUE, DEEMED));

        DivorceServiceApplication result = getLastServiceApplication(
            ImmutableMap.of(CcdFields.SERVICE_APPLICATIONS, myList)
        );

        assertThat(result.getType(), is(DEEMED));
    }

    @Test
    public void getLastServiceApplicationShouldReturnLastElementWhenManyElementsOnList() {
        final List<CollectionMember<DivorceServiceApplication>> myList = asList(
            buildCollectionMember(YES_VALUE, DEEMED),
            buildCollectionMember(YES_VALUE, DISPENSED),
            buildCollectionMember(NO_VALUE, DISPENSED),
            buildCollectionMember(NO_VALUE, DEEMED)
        );

        DivorceServiceApplication result = getLastServiceApplication(
            ImmutableMap.of(CcdFields.SERVICE_APPLICATIONS, myList)
        );

        assertThat(result.getApplicationGranted(), is(NO_VALUE));
        assertThat(result.getType(), is(DEEMED));
    }

    public static CollectionMember<DivorceServiceApplication> buildCollectionMember(String granted, String type) {
        CollectionMember<DivorceServiceApplication> collectionMember = new CollectionMember<>();
        collectionMember.setValue(buildServiceApplication(granted, type));

        return collectionMember;
    }

    private static DivorceServiceApplication buildServiceApplication(String granted, String type) {
        return DivorceServiceApplication.builder()
            .applicationGranted(granted)
            .type(type)
            .build();
    }

    private static Map<String, Object> buildCaseDataWithField(String field, String value) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(field, value);

        return caseData;
    }
}
