package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FieldsRemovalTask;

import java.util.List;

import static java.util.Arrays.asList;

@Component
@Slf4j
public class ServiceApplicationRemovalTask extends FieldsRemovalTask {

    @Override
    protected List<String> getFieldsToRemove() {
        return asList(
            CcdFields.RECEIVED_SERVICE_APPLICATION_DATE,
            CcdFields.RECEIVED_SERVICE_ADDED_DATE,
            CcdFields.SERVICE_APPLICATION_TYPE,
            CcdFields.SERVICE_APPLICATION_PAYMENT,
            CcdFields.SERVICE_APPLICATION_GRANTED,
            CcdFields.SERVICE_APPLICATION_DECISION_DATE,
            CcdFields.SERVICE_APPLICATION_REFUSAL_REASON
        );
    }
}
