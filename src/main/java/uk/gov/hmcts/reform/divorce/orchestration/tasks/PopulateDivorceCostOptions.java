package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PopulateDivorceCostOptions implements Task<Map<String, Object>> {

    private static final String ORIGINAL_AMOUNT = "originalAmount";
    private static final String COUNTER_OFFER = "counterOffer";
    private static final String END_CLAIM = "endClaim";

    private static final String RESP_AGREE_TO_COSTS = "RespAgreeToCosts";
    private static final String DIFFERENT_AMOUNT = "DifferentAmount";
    private static final String RESP_COSTS_AMOUNT = "RespCostsAmount";

    private static final String SELECTED_ITEM = "selectedItem";
    private static final String OPTIONS = "options";
    private static final String DIVORCE_COSTS_OPTION_DN_ENUM = "DivorceCostsOptionDNEnum";

    private static final String ORIGINAL_AMOUNT_OPTION = "The petition still wants to claim costs and will let the court decide how much";
    private static final String COUNTER_OFFER_OPTION = "The petitioner will accept the amount proposed by the respondent (£%s)";
    private static final String END_CLAIM_OPTION = "The petitioner does not want to claim costs anymore";

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {

        Map<String, String> options = new LinkedHashMap<>();
        options.put(ORIGINAL_AMOUNT, ORIGINAL_AMOUNT_OPTION);
        String respondentCostAmount = (String) payload.get(RESP_COSTS_AMOUNT);

        if (DIFFERENT_AMOUNT.equals(payload.get(RESP_AGREE_TO_COSTS))) {
            options.put(COUNTER_OFFER,
                    String.format(COUNTER_OFFER_OPTION,
                            respondentCostAmount)
            );
        }

        options.put(END_CLAIM, END_CLAIM_OPTION);

        Map.Entry<String, String> entry = options.entrySet().iterator().next();
        String firstOptionKey = entry.getKey();

        Map<String, Object> dnCostsMap = new HashMap<>();
        dnCostsMap.put(SELECTED_ITEM, firstOptionKey);
        dnCostsMap.put(OPTIONS, options);

        payload.put(DIVORCE_COSTS_OPTION_DN_ENUM, dnCostsMap);

        return payload;
    }
}
