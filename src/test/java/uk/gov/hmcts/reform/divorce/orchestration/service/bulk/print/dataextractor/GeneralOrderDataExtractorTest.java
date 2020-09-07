package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.Map;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;
import static java.util.Collections.EMPTY_MAP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GeneralOrderDataExtractorTest {

    public static final String VALUE = "expected value";
    public static final String CCD_DATE = "2000-10-01";

    @Test
    public void getJudgeNameShouldReturnValidValue() {
        Map<String, Object> caseData = ImmutableMap.of(CcdFields.JUDGE_NAME, VALUE);
        assertThat(GeneralOrderDataExtractor.getJudgeName(caseData), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getJudgeNameShouldThrowException() {
        GeneralOrderDataExtractor.getJudgeName(EMPTY_MAP);
    }

    @Test
    public void getJudgeTypeShouldReturnValidValue() {
        Map<String, Object> caseData = ImmutableMap.of(CcdFields.JUDGE_TYPE, VALUE);
        assertThat(GeneralOrderDataExtractor.getJudgeType(caseData), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getJudgeTypeShouldThrowException() {
        GeneralOrderDataExtractor.getJudgeType(EMPTY_MAP);
    }

    @Test
    public void getGeneralOrderDateShouldReturnValidValue() {
        Map<String, Object> caseData = ImmutableMap.of(CcdFields.GENERAL_ORDER_DATE, CCD_DATE);
        assertThat(GeneralOrderDataExtractor.getGeneralOrderDate(caseData), is("1 October 2000"));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getGeneralOrderDateShouldThrowException() {
        GeneralOrderDataExtractor.getGeneralOrderDate(EMPTY_MAP);
    }

    @Test
    public void getGeneralOrderDetailsShouldReturnValidValue() {
        Map<String, Object> caseData = ImmutableMap.of(CcdFields.GENERAL_ORDER_DETAILS, VALUE);
        assertThat(GeneralOrderDataExtractor.getGeneralOrderDetails(caseData), is(VALUE));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getGeneralOrderDetailsShouldThrowException() {
        GeneralOrderDataExtractor.getGeneralOrderDetails(EMPTY_MAP);
    }

    @Test
    public void getGeneralOrderRecitalsShouldReturnValidValue() {
        Map<String, Object> caseData = ImmutableMap.of(CcdFields.GENERAL_ORDER_RECITALS, VALUE);
        assertThat(GeneralOrderDataExtractor.getGeneralOrderRecitals(caseData), is(VALUE));
    }

    @Test
    public void getGeneralOrderRecitalsShouldReturnEmptyString() {
        assertThat(GeneralOrderDataExtractor.getGeneralOrderRecitals(EMPTY_MAP), is(EMPTY_STRING));
    }
}
