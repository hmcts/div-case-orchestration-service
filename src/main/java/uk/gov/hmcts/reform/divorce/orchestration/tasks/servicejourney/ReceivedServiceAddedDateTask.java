package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.DateFieldSetupTask;

@Component
@RequiredArgsConstructor
public class ReceivedServiceAddedDateTask extends DateFieldSetupTask {

    public static final String RECEIVED_SERVICE_ADDED_DATE = "ReceivedServiceAddedDate";

    @Override
    protected String getFieldName() {
        return RECEIVED_SERVICE_ADDED_DATE;
    }
}
