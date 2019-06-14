package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DNCostOptionsEnum;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicList;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicListItem;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COST_OPTIONS_DN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AGREE_TO_COSTS;

@RunWith(MockitoJUnitRunner.class)
public class SetDNCostOptionsTest {

    private static final String FIXED_DATE = "2019-05-11";

    @InjectMocks
    private SetDNcostOptions setDNcostOptions;

    private Map<String, Object> testData;
    private TaskContext context;

    @Before
    public void setup() {
        testData = new HashMap<>();
        context = new DefaultTaskContext();
    }

    @Test
    public void whenRespAgreeToCosts_doNotShowDifferentAmountInCostOptions() throws TaskException {

        Map<String, Object> resultMap = setDNcostOptions.execute(context, testData);

        List<DynamicListItem> listCostOptions  = new ArrayList<>();
        listCostOptions.add(
            new DynamicListItem(DNCostOptionsEnum.ORIGINALAMOUNT.getCode(), DNCostOptionsEnum.ORIGINALAMOUNT.getLabel())
        );
        listCostOptions.add(
            new DynamicListItem(DNCostOptionsEnum.ENDCLAIM.getCode(), DNCostOptionsEnum.ENDCLAIM.getLabel())
        );

        assertThat(resultMap, allOf(
            hasEntry(is(DIVORCE_COST_OPTIONS_DN), is(new DynamicList(listCostOptions.get(0), listCostOptions))))
        );
    }

    @Test
    public void whenRespNotAgreeToCosts_doNotShowDifferentAmountInCostOptions() throws TaskException {

        testData.put(RESP_AGREE_TO_COSTS, NO_VALUE);

        List<DynamicListItem> listCostOptions  = new ArrayList<>();
        listCostOptions.add(
            new DynamicListItem(DNCostOptionsEnum.ORIGINALAMOUNT.getCode(), DNCostOptionsEnum.ORIGINALAMOUNT.getLabel())
        );
        listCostOptions.add(
            new DynamicListItem(DNCostOptionsEnum.DIFFERENTAMOUNT.getCode(), DNCostOptionsEnum.DIFFERENTAMOUNT.getLabel())
        );
        listCostOptions.add(
            new DynamicListItem(DNCostOptionsEnum.ENDCLAIM.getCode(), DNCostOptionsEnum.ENDCLAIM.getLabel())
        );

        Map<String, Object> resultMap = setDNcostOptions.execute(context, testData);
        assertThat(resultMap, allOf(
            hasEntry(is(DIVORCE_COST_OPTIONS_DN), is(new DynamicList(listCostOptions.get(0), listCostOptions))))
        );
    }

}
