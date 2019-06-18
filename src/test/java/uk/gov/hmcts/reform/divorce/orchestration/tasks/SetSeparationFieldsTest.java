package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_DESERTION_TIME_TOGETHER_PERMITTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_MENTAL_SEP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PHYSICAL_SEP_DAIE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_DESERTION_DAIE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE_SEP_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_SEP_REF_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_SEP_TIME_TOGETHER_PERMITTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_2YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEPARATION_5YRS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SEP_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.VALIDATION_ERROR_KEY;

@RunWith(MockitoJUnitRunner.class)
public class SetSeparationFieldsTest {

    private static final String FIXED_DATE = "2019-05-11";

    @InjectMocks
    private SetSeparationFields setSeparationFields;

    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = new HashMap<>();
        context = new DefaultTaskContext();
    }

    @Test
    public void whenSep5Yrs_datesMoreThan5YrsInPast_calledExecuteShouldSetCalculatedDetailsOnPayload() throws TaskException {

        String pastDate5Yrs8Mnths = DateUtils.formatDateFromDateTime(LocalDateTime.now().minusYears(5).minusMonths(8));
        String pastDate5Yrs9Mnths = DateUtils.formatDateFromDateTime(LocalDateTime.now().minusYears(5).minusMonths(9));

        testData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_5YRS);
        testData.put(D_8_MENTAL_SEP_DATE, pastDate5Yrs8Mnths);
        testData.put(D_8_PHYSICAL_SEP_DAIE, pastDate5Yrs9Mnths);

        String pastDate5Yrs6Mnths = DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now().minusYears(5).minusMonths(6));

        Map<String, Object> resultMap = setSeparationFields.execute(context, testData);

        assertThat(resultMap, allOf(
            hasEntry(is(D_8_SEP_TIME_TOGETHER_PERMITTED), is("6 months")),
            hasEntry(is(D_8_REASON_FOR_DIVORCE_SEP_DATE), is(pastDate5Yrs8Mnths)),
            hasEntry(is(D_8_SEP_REF_DATE), is(pastDate5Yrs6Mnths)),
            hasEntry(is(SEP_YEARS), is("5"))
        ));
    }

    @Test
    public void whenSep5Yrs_datesLessThan5Yrs_executeShouldSetCalculatedDetailsOnPayload() throws TaskException {

        String pastDate5Yrs8Mnths = DateUtils.formatDateFromDateTime(LocalDateTime.now().minusYears(5).minusMonths(8));
        String todayDate = DateUtils.formatDateFromDateTime(LocalDateTime.now());

        testData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_5YRS);
        testData.put(D_8_MENTAL_SEP_DATE, pastDate5Yrs8Mnths);
        testData.put(D_8_PHYSICAL_SEP_DAIE, todayDate);

        setSeparationFields.execute(context, testData);
        assertThat(context.hasTaskFailed(), is(true));
        assertThat(context.getTransientObject(VALIDATION_ERROR_KEY), is(setSeparationFields.FACT_CANT_USE));
    }

    @Test
    public void whenSep2Yr_executeShouldSetCalculatedDetailsOnPayload() throws TaskException {

        String pastDate2Yrs8Mnths = DateUtils.formatDateFromDateTime(LocalDateTime.now().minusYears(2).minusMonths(8));
        String pastDate2Yrs9Mnths = DateUtils.formatDateFromDateTime(LocalDateTime.now().minusYears(2).minusMonths(9));

        testData.put(D_8_REASON_FOR_DIVORCE, SEPARATION_2YRS);
        testData.put(D_8_MENTAL_SEP_DATE, pastDate2Yrs8Mnths);
        testData.put(D_8_PHYSICAL_SEP_DAIE, pastDate2Yrs9Mnths);

        String pastDate2Yrs6Mnths = DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now().minusYears(2).minusMonths(6));

        Map<String, Object> resultMap = setSeparationFields.execute(context, testData);

        assertThat(resultMap, allOf(
            hasEntry(is(D_8_SEP_TIME_TOGETHER_PERMITTED), is("6 months")),
            hasEntry(is(D_8_REASON_FOR_DIVORCE_SEP_DATE), is(pastDate2Yrs8Mnths)),
            hasEntry(is(D_8_SEP_REF_DATE), is(pastDate2Yrs6Mnths)),
            hasEntry(is(SEP_YEARS), is("2"))
        ));
    }

    @Test
    public void whenDesertion_executeShouldSetCalculatedDetailsOnPayload() throws TaskException {

        String pastDate2Yrs6MnthsPlus1day = DateUtils.formatDateFromDateTime(LocalDateTime.now().minusYears(2).minusMonths(6).plusDays(1));
        String pastDate2Yrs6MnthsPlus1dayInClientFormat = DateUtils.formatDateWithCustomerFacingFormat(
            LocalDate.now().minusYears(2).minusMonths(6).plusDays(1)
        );
        testData.put(D_8_REASON_FOR_DIVORCE, DESERTION);
        testData.put(D_8_REASON_FOR_DIVORCE_DESERTION_DAIE, pastDate2Yrs6MnthsPlus1day);

        String pastDate2Yrs6Mnths = DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now().minusYears(2).minusMonths(6));


        Map<String, Object> resultMap = setSeparationFields.execute(context, testData);

        assertThat(resultMap, allOf(
            hasEntry(is(D_8_DESERTION_TIME_TOGETHER_PERMITTED), is("25 weeks and 6 days")),
            hasEntry(is(D_8_REASON_FOR_DIVORCE_SEP_DATE), is(pastDate2Yrs6MnthsPlus1day)),
            hasEntry(is(D_8_SEP_REF_DATE), is(pastDate2Yrs6MnthsPlus1dayInClientFormat)),
            hasEntry(is(SEP_YEARS), is("2"))
        ));
    }
}
