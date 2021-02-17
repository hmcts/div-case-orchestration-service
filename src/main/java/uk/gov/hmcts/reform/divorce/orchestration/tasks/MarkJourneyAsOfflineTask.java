package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.DIVORCE_PARTY;

@Component
public class MarkJourneyAsOfflineTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        DivorceParty divorceParty = context.getTransientObject(DIVORCE_PARTY);

        if (RESPONDENT.equals(divorceParty)) {
            payload.put(RESP_IS_USING_DIGITAL_CHANNEL, NO_VALUE);
        } else if (CO_RESPONDENT.equals(divorceParty)) {
            payload.put(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL, NO_VALUE);
        }

        return payload;
    }
}
