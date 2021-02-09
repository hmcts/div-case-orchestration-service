package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.DateFieldSetupTask;

@Component
@RequiredArgsConstructor
public class ReceivedServiceAddedDateTask extends DateFieldSetupTask {

    @Override
    protected String getFieldName() {
        return CcdFields.RECEIVED_SERVICE_ADDED_DATE;
    }
}
