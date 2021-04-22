package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FieldsRemovalTask;

import java.util.List;

import static java.util.Arrays.asList;

@Component
@Slf4j
public class JudgeCostsClaimFieldsRemovalTask extends FieldsRemovalTask {

    @Override
    protected List<String> getFieldsToRemove() {
        return asList(
            CcdFields.JUDGE_COSTS_CLAIM_GRANTED,
            CcdFields.JUDGE_WHO_PAYS_COSTS,
            CcdFields.JUDGE_TYPE_COSTS_DECISION,
            CcdFields.JUDGE_COSTS_ADDITIONAL_INFO
        );
    }
}
