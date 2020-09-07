package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.RemovalFieldTask;

@Component
@Slf4j
public class GeneralOrderDraftRemovalTask extends RemovalFieldTask {

    @Override
    protected String getFieldToRemove() {
        return CcdFields.GENERAL_ORDER_DRAFT;
    }
}
