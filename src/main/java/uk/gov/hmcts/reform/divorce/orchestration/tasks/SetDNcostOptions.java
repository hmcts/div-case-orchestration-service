package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DNCostOptionsEnum;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicList;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicListItem;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COST_OPTIONS_DN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AGREE_TO_COSTS;

@Component
public class SetDNcostOptions implements Task<Map<String,Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        String respAgreeToCosts = getFieldAsStringOrNull(caseData, RESP_AGREE_TO_COSTS);
        List<DynamicListItem> listCostOptions  = new ArrayList<>();
        listCostOptions.add(
            new DynamicListItem(DNCostOptionsEnum.ORIGINALAMOUNT.getCode(), DNCostOptionsEnum.ORIGINALAMOUNT.getLabel())
        );
        if (StringUtils.equalsIgnoreCase(respAgreeToCosts, NO_VALUE)) {
            listCostOptions.add(
                new DynamicListItem(DNCostOptionsEnum.DIFFERENTAMOUNT.getCode(), DNCostOptionsEnum.DIFFERENTAMOUNT.getLabel())
            );
        }
        listCostOptions.add(
            new DynamicListItem(DNCostOptionsEnum.ENDCLAIM.getCode(), DNCostOptionsEnum.ENDCLAIM.getLabel())
        );

        caseData.put(DIVORCE_COST_OPTIONS_DN, new DynamicList(listCostOptions.get(0), listCostOptions));
        return caseData;
    }

    private String getFieldAsStringOrNull(final Map<String, Object>  caseData, String fieldKey) {
        Object fieldValue = caseData.get(fieldKey);
        if (fieldValue == null) {
            return null;
        }
        return fieldValue.toString();
    }
}