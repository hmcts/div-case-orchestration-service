package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FieldsRemovalTask;

import java.util.List;

import static java.util.Arrays.asList;

@Component
@Slf4j
public class BailiffServiceApplicationRemovalTask extends FieldsRemovalTask {

    @Override
    protected List<String> getFieldsToRemove() {
        return asList(
            CcdFields.RECEIVED_SERVICE_APPLICATION_DATE,
            CcdFields.SERVICE_APPLICATION_TYPE,
            CcdFields.SERVICE_APPLICATION_PAYMENT,
            CcdFields.SERVICE_APPLICATION_GRANTED,
            CcdFields.SERVICE_APPLICATION_DECISION_DATE,
            CcdFields.SERVICE_APPLICATION_REFUSAL_REASON,
            CcdFields.LOCAL_COURT_DETAILS_LABEL,
            CcdFields.LOCAL_COURT_ADDRESS,
            CcdFields.LOCAL_COURT_EMAIL,
            CcdFields.BAILIFF_RETURN_LABEL,
            CcdFields.CERTIFICATE_OF_SERVICE_DATE,
            CcdFields.BAILIFF_SERVICE_SUCCESSFUL,
            CcdFields.REASON_FAILURE_TO_SERVE
        );
    }
}
