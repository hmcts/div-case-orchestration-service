package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PopulateDivorceCostOptions implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {

        Map<String, String> options = new LinkedHashMap<>();
        options.put("originalAmount", "The petition still wants to claim costs and will let the court decide how much");
        String respondentCostAmount = (String) payload.get("RespCostsAmount");
        options.put("counterOffer",
                String.format("The petitioner will accept the amount proposed by the respondent (£%s)",
                        respondentCostAmount)
        );
        options.put("endClaim", "The petitioner does not want to claim costs anymore");

        Map.Entry<String, String> entry = options.entrySet().iterator().next();
        String key = entry.getKey();

        Map<String, Object> dnCostsMap = new HashMap<>();
        dnCostsMap.put("selectedItem", key);
        dnCostsMap.put("options", options);

        payload.put("DivorceCostsOptionDNEnum", dnCostsMap);

        return payload;
    }
}
