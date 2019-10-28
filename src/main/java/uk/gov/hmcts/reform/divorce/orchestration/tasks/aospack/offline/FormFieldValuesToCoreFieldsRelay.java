package uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_2_YR_CONSENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_AOS_ADMIT_ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.UI_ONLY_RESP_WILL_DEFEND_DIVORCE;

/**
 * Some fields were created in the CCD form for usability reasons, but they need to have
 * their values relayed to core CCD fields so that the actual information can have significance.
 */
@Component
public class FormFieldValuesToCoreFieldsRelay implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        Map<String, Object> payloadToReturn = new HashMap<>(payload);

        Optional.ofNullable(payload.get(RESP_AOS_2_YR_CONSENT)).ifPresent(relayValueToField(payloadToReturn, RESP_ADMIT_OR_CONSENT_TO_FACT));
        Optional.ofNullable(payload.get(RESP_AOS_ADMIT_ADULTERY)).ifPresent(relayValueToField(payloadToReturn, RESP_ADMIT_OR_CONSENT_TO_FACT));

        Optional.ofNullable(payload.get(UI_ONLY_RESP_WILL_DEFEND_DIVORCE)).ifPresent(relayValueToField(payloadToReturn, RESP_WILL_DEFEND_DIVORCE));

        return payloadToReturn;
    }

    private Consumer<Object> relayValueToField(Map<String, Object> payloadToReturn, String newFieldName) {
        return value -> payloadToReturn.put(newFieldName, value);
    }

}
