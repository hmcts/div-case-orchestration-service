package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.ReferenceNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember.buildCollectionMember;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.FeesAndPaymentHelper.buildFurtherPaymentData;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;


@RunWith(MockitoJUnitRunner.class)
public abstract class FurtherPaymentTaskTest {

    private static final String OTHER_REF_NUMBER = "other_ref_number";

    private static final String TEST_REF_NUMBER = "test_ref_number";

    protected abstract FurtherPaymentTask getTask();

    protected abstract String getFurtherPaymentReferenceNumbersField();

    protected abstract String getPaymentReferenceNumberField();

    protected abstract String getPaymentType();

    @Test
    public void shouldAddOneItemToPaymentReferenceCollectionIfNonExists() {
        Map<String, Object> caseData = buildFurtherPaymentData(getPaymentType(), TEST_REF_NUMBER);

        Map<String, Object> resultData = getTask().execute(context(), caseData);

        List<CollectionMember<ReferenceNumber>> collectionMembers = getFurtherPaymentReferenceCollection(resultData);
        ReferenceNumber referenceNumber = collectionMembers.get(0).getValue();

        assertThat(collectionMembers, hasSize(1));
        assertThat(resultData.get(getPaymentReferenceNumberField()), is(nullValue()));
        assertThat(referenceNumber.getReference(), is(TEST_REF_NUMBER));
    }

    @Test
    public void shouldAddToExistingPaymentReferenceCollection() {
        List<CollectionMember<ReferenceNumber>> furtherReferenceNumbers = new ArrayList<>(
            asList(buildCollectionMember(ReferenceNumber.builder()
                .reference(OTHER_REF_NUMBER)
                .build()))
        );

        Map<String, Object> caseData = buildFurtherPaymentData(getPaymentType(), TEST_REF_NUMBER);
        caseData.put(getFurtherPaymentReferenceNumbersField(), furtherReferenceNumbers);

        Map<String, Object> resultData = getTask().execute(context(), caseData);

        List<CollectionMember<ReferenceNumber>> collectionMembers = getFurtherPaymentReferenceCollection(resultData);
        ReferenceNumber referenceNumber = collectionMembers.get(1).getValue();

        assertThat(collectionMembers, hasSize(2));
        assertThat(resultData.get(getPaymentReferenceNumberField()), is(nullValue()));
        assertThat(referenceNumber.getReference(), is(TEST_REF_NUMBER));
    }

    @Test
    public void shouldDoNothingWhenNoReferenceNumberExists() {
        Map<String, Object> caseData = buildFurtherPaymentData(getPaymentType(),null);

        Map<String, Object> resultData = getTask().execute(context(), caseData);

        assertThat(resultData.get(getPaymentReferenceNumberField()), is(nullValue()));
        assertThat(resultData.get(getFurtherPaymentReferenceNumbersField()), is(nullValue()));
    }

    private List getFurtherPaymentReferenceCollection(Map<String, Object> resultData) {
        return (List) resultData.get(getFurtherPaymentReferenceNumbersField());
    }

}