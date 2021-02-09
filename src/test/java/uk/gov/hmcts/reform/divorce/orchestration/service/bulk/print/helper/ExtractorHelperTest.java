package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryListOfStrings;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.ExtractorHelper.getMandatoryStringValue;

public class ExtractorHelperTest {

    public static final String FIELD = "field";
    public static final String VALUE = "value";

    @Test
    public void getMandatoryStringValueReturnsValueWhenFieldExists() {
        Map<String, Object> caseData = ImmutableMap.of(FIELD, VALUE);

        assertThat(getMandatoryStringValue(caseData, FIELD), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getMandatoryStringValueThrowsInvalidDataForTaskExceptionWhenNullNull() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FIELD, null);

        getMandatoryStringValue(caseData, FIELD);
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getMandatoryStringValueThrowsInvalidDataForTaskExceptionWhenEmpty() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FIELD, EMPTY_STRING);

        getMandatoryStringValue(caseData, FIELD);
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getMandatoryStringValueThrowsInvalidDataForTaskExceptionWhenFieldDoesntExist() {
        Map<String, Object> caseData = new HashMap<>();

        getMandatoryStringValue(caseData, FIELD);
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getMandatoryListOfStringsThrowsInvalidDataForTaskExceptionWhenEmptyMap() {
        Map<String, Object> caseData = new HashMap<>();

        getMandatoryListOfStrings(caseData, FIELD);
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getMandatoryListOfStringsThrowsInvalidDataForTaskExceptionWhenFieldNotFound() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("some other field", emptyList());

        getMandatoryListOfStrings(caseData, FIELD);
    }

    @Test(expected = ClassCastException.class)
    public void getMandatoryListOfStringsThrowsClassCastExceptionWhenInvalidType() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FIELD, new HashMap<>());

        getMandatoryListOfStrings(caseData, FIELD);
    }

    @Test(expected = ClassCastException.class)
    public void getMandatoryListOfStringsThrowsClassCastExceptionWhenSetProvided() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FIELD, new HashSet<>(asList("a", "b", "c")));

        getMandatoryListOfStrings(caseData, FIELD);
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getMandatoryListOfStringsThrowsInvalidDataForTaskExceptionWhenNull() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FIELD, null);

        getMandatoryListOfStrings(caseData, FIELD);
    }

    @Test
    public void getMandatoryListOfStringsReturnsEmptyListWhenEmptyList() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FIELD, emptyList());

        assertThat(getMandatoryListOfStrings(caseData, FIELD), is(emptyList()));
    }

    @Test
    public void getMandatoryListOfStringsReturnsPopulatedListWhenListProvided() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FIELD, asList("a", "b", "c"));

        assertThat(getMandatoryListOfStrings(caseData, FIELD).size(), is(3));
    }
}
