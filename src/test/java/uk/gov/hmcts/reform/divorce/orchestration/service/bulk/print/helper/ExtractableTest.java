package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.Extractable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExtractableTest {

    public static final String FIELD = "field";
    public static final String VALUE = "value";

    private Extractable extractable = new Extractable() {
    };

    @Test
    public void getMandatoryStringValueReturnsValueWhenFieldExists() {
        Map<String, Object> caseData = ImmutableMap.of(FIELD, VALUE);

        assertThat(extractable.getMandatoryStringValue(caseData, FIELD), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getMandatoryStringValueThrowsInvalidDataForTaskExceptionWhenNullNull() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FIELD, null);

        extractable.getMandatoryStringValue(caseData, FIELD);
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getMandatoryStringValueThrowsInvalidDataForTaskExceptionWhenEmpty() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FIELD, EMPTY_STRING);

        extractable.getMandatoryStringValue(caseData, FIELD);
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getMandatoryStringValueThrowsInvalidDataForTaskExceptionWhenFieldDoesntExist() {
        Map<String, Object> caseData = new HashMap<>();

        extractable.getMandatoryStringValue(caseData, FIELD);
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getMandatoryListOfStringsThrowsInvalidDataForTaskExceptionWhenEmptyMap() {
        Map<String, Object> caseData = new HashMap<>();

        extractable.getMandatoryListOfStrings(caseData, FIELD);
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getMandatoryListOfStringsThrowsInvalidDataForTaskExceptionWhenFieldNotFound() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("some other field", emptyList());

        extractable.getMandatoryListOfStrings(caseData, FIELD);
    }

    @Test(expected = ClassCastException.class)
    public void getMandatoryListOfStringsThrowsClassCastExceptionWhenInvalidType() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FIELD, new HashMap<>());

        extractable.getMandatoryListOfStrings(caseData, FIELD);
    }

    @Test(expected = ClassCastException.class)
    public void getMandatoryListOfStringsThrowsClassCastExceptionWhenSetProvided() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FIELD, new HashSet<>(asList("a", "b", "c")));

        extractable.getMandatoryListOfStrings(caseData, FIELD);
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getMandatoryListOfStringsThrowsInvalidDataForTaskExceptionWhenNull() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FIELD, null);

        extractable.getMandatoryListOfStrings(caseData, FIELD);
    }

    @Test
    public void getMandatoryListOfStringsReturnsEmptyListWhenEmptyList() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FIELD, emptyList());

        assertThat(extractable.getMandatoryListOfStrings(caseData, FIELD), is(emptyList()));
    }

    @Test
    public void getMandatoryListOfStringsReturnsPopulatedListWhenListProvided() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(FIELD, asList("a", "b", "c"));

        assertThat(extractable.getMandatoryListOfStrings(caseData, FIELD).size(), is(3));
    }
}
