package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.RemovalFieldTask;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_REFUSAL_DRAFT;

@Component
@Slf4j
public class ServiceRefusalDraftRemovalTask extends RemovalFieldTask {

    @Override
    protected String getFieldToRemove() {
        return SERVICE_REFUSAL_DRAFT;
    }
}
