package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.HashMap;
import java.util.Map;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
}