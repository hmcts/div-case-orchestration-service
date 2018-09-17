package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATEMENT_OF_TRUTH;

@Component
public class ValidateSolicitorCaseData implements Task<Map<String, Object>> {

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        boolean petitionStatmentOfTruth = valueEqualsYes((String) caseData.get(STATEMENT_OF_TRUTH));
        boolean solStatmentOfTruth = valueEqualsYes((String) caseData.get(SOLICITOR_STATEMENT_OF_TRUTH));

        if (!petitionStatmentOfTruth || !solStatmentOfTruth) {
            context.setTaskFailed(true);
            context.setTransientObject(this.getClass().getName() + "_Error",
                    Collections.singletonList("Statement of truth for solicitor and petitioner needs to be accepted"));
        }

        return caseData;
    }

    private boolean valueEqualsYes(String value) {
        return Optional.ofNullable(value)
                .map(i -> "YES".equalsIgnoreCase(i))
                .orElse(false);
    }
}